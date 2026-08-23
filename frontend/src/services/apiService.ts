import type { Assignee, AssigneeCreateUpdate } from '@/types/assignee';
import type { Todo, TodoCreateUpdate, TodoStatus, Priority } from '@/types/todo';
import type { TodoStats } from '@/types/todoStats';
import type { PriorityCorrectionStats } from '@/types/priorityCorrections';

const API_BASE_URL = '/api/v1';

/**
 * Parses a fetch response, rejecting with an Error when the request failed.
 *
 * Handles JSON and empty/non-JSON responses uniformly so callers only have to
 * await the promise and catch errors.
 *
 * @param response - the raw fetch Response to inspect
 * @returns the parsed JSON body, or an empty object for non-JSON responses
 * @throws {Error} when the response status is not ok
 */
async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API request failed with status ${response.status}`);
  }

  // Only parse as JSON when the server advertises a JSON content type.
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json() as Promise<T>;
  }

  // No content or non-JSON: return an empty object cast to the expected type.
  return {} as T;
}

// --- Assignee API Calls ---

/**
 * Fetches all assignees from the backend.
 *
 * @returns a list of assignees; empty array when the API returns no results
 * @throws {Error} when the request fails or the backend returns a non-ok status
 */
export async function getAssignees(): Promise<Assignee[]> {
  const response = await fetch(`${API_BASE_URL}/assignees`);
  return await handleResponse<Assignee[]>(response);
}

/**
 * Fetches a single assignee by its identifier.
 *
 * @param id - the assignee identifier
 * @returns the matching assignee
 * @throws {Error} when the request fails or the assignee does not exist
 */
export async function getAssignee(id: number): Promise<Assignee> {
  const response = await fetch(`${API_BASE_URL}/assignees/${id}`);
  return await handleResponse<Assignee>(response);
}

/**
 * Creates a new assignee.
 *
 * @param assignee - the assignee data to create
 * @returns the created assignee with its generated id
 * @throws {Error} when the request fails or validation fails on the backend
 */
export async function createAssignee(assignee: AssigneeCreateUpdate): Promise<Assignee> {
  const response = await fetch(`${API_BASE_URL}/assignees`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(assignee),
  });
  return await handleResponse<Assignee>(response);
}

/**
 * Updates an existing assignee.
 *
 * @param id - the identifier of the assignee to update
 * @param assignee - the new assignee data
 * @returns the updated assignee
 * @throws {Error} when the request fails or the assignee does not exist
 */
export async function updateAssignee(id: number, assignee: AssigneeCreateUpdate): Promise<Assignee> {
  const response = await fetch(`${API_BASE_URL}/assignees/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(assignee),
  });
  return await handleResponse<Assignee>(response);
}

/**
 * Deletes an assignee by its identifier.
 *
 * @param id - the identifier of the assignee to delete
 * @throws {Error} when the request fails or the assignee does not exist
 */
export async function deleteAssignee(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/assignees/${id}`, {
    method: 'DELETE',
  });
  // DELETE often returns no content; just check whether the response is ok.
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API delete request failed with status ${response.status}`);
  }
}

// --- Todo API Calls ---

/**
 * Fetches all todos from the backend.
 *
 * @returns a list of todos; empty array when the API returns no results
 * @throws {Error} when the request fails or the backend returns a non-ok status
 */
export async function getTodos(): Promise<Todo[]> {
  const response = await fetch(`${API_BASE_URL}/todos`);
  return await handleResponse<Todo[]>(response);
}

/**
 * Fetches a single todo by its identifier.
 *
 * @param id - the todo identifier
 * @returns the matching todo
 * @throws {Error} when the request fails or the todo does not exist
 */
export async function getTodo(id: number): Promise<Todo> {
  const response = await fetch(`${API_BASE_URL}/todos/${id}`);
  return await handleResponse<Todo>(response);
}

/**
 * Creates a new todo.
 *
 * @param todo - the todo data to create
 * @returns the created todo with its generated id
 * @throws {Error} when the request fails or validation fails on the backend
 */
export async function createTodo(todo: TodoCreateUpdate): Promise<Todo> {
  const response = await fetch(`${API_BASE_URL}/todos`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(todo),
  });
  return await handleResponse<Todo>(response);
}

/**
 * Updates an existing todo.
 *
 * @param id - the identifier of the todo to update
 * @param todo - the new todo data
 * @returns the updated todo
 * @throws {Error} when the request fails or the todo does not exist
 */
export async function updateTodo(id: number, todo: TodoCreateUpdate): Promise<Todo> {
  const response = await fetch(`${API_BASE_URL}/todos/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(todo),
  });
  return await handleResponse<Todo>(response);
}

/**
 * Transitions a todo to a new Kanban workflow status.
 *
 * @param id - the identifier of the todo to transition
 * @param status - the target workflow status (OPEN, IN_PROGRESS or DONE)
 * @returns the updated todo
 * @throws {Error} when the request fails, the todo does not exist or the status is invalid
 */
export async function transitionTodoStatus(id: number, status: TodoStatus): Promise<Todo> {
  const response = await fetch(`${API_BASE_URL}/todos/${id}/status`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(status),
  });
  return await handleResponse<Todo>(response);
}

/**
 * Deletes a todo by its identifier.
 *
 * @param id - the identifier of the todo to delete
 * @throws {Error} when the request fails or the todo does not exist
 */
export async function deleteTodo(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/todos/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `API delete request failed with status ${response.status}`);
  }
}

/**
 * Fetches aggregated todo statistics from the backend.
 *
 * @returns completion rate, average days to finish, and per-priority,
 *     per-category and per-assignee counts
 * @throws {Error} when the request fails or the backend returns a non-ok status
 */
export async function getTodoStats(): Promise<TodoStats> {
  const response = await fetch(`${API_BASE_URL}/todos/stats`);
  return await handleResponse<TodoStats>(response);
}

/**
 * Records a priority correction from classifier feedback (thumbs-down).
 *
 * @param todoTitle - the title of the todo the prediction was made for
 * @param predictedPriority - the priority the system had assigned
 * @param correctedPriority - the priority the user says is correct
 * @param category - optional category of the todo at correction time
 * @throws {Error} when the request fails or validation fails on the backend
 */
export async function createPriorityCorrection(
  todoTitle: string,
  predictedPriority: Priority,
  correctedPriority: Priority,
  category?: string | null,
): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/todos/priority-corrections`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      todoTitle,
      predictedPriority,
      correctedPriority,
      category: category ?? null,
    }),
  });
  await handleResponse<unknown>(response);
}

/**
 * Fetches aggregated priority-correction statistics.
 *
 * @returns total corrections plus counts per transition and per target priority
 * @throws {Error} when the request fails or the backend returns a non-ok status
 */
export async function getPriorityCorrectionStats(): Promise<PriorityCorrectionStats> {
  const response = await fetch(`${API_BASE_URL}/todos/priority-corrections/stats`);
  return await handleResponse<PriorityCorrectionStats>(response);
}

// --- CSV Download API Calls ---

/**
 * Downloads all todos as a CSV blob from the backend.
 *
 * @returns a Blob containing the todos in CSV format
 * @throws {Error} when the request fails or the backend returns a non-ok status
 */
export async function downloadTodosCsv(): Promise<Blob> {
  const response = await fetch(`${API_BASE_URL}/csv-downloads/todos`);
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `CSV download failed with status ${response.status}`);
  }
  return await response.blob();
}
