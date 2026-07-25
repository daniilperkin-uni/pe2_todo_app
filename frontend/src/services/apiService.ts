import type { Assignee, AssigneeCreateUpdate } from '@/types/assignee';
import type { Todo, TodoCreateUpdate } from '@/types/todo';

const API_BASE_URL = '/api/v1';

// --- OFFLINE FALLBACK DATA ---
const OFFLINE_ASSIGNEES: Assignee[] = [
    { id: 1, prename: 'Max', name: 'Mustermann', email: 'max.mustermann@uni-stuttgart.de' },
    { id: 2, prename: 'Julia', name: 'Meier', email: 'julia.meier@uni-stuttgart.de' },
];

const OFFLINE_TODOS: Todo[] = [
    {
        id: 1,
        title: 'Einkaufen',
        description: 'Milch kaufen',
        finished: false,
        priority: 'MEDIUM',
        assigneeList: [OFFLINE_ASSIGNEES[0]!],
        createdDate: '2023-11-20',
        dueDate: '2023-12-01',
        finishedDate: ''
    },
    {
        id: 2,
        title: 'Projektbericht schreiben',
        description: 'Abschlussbericht für PE2',
        finished: false,
        priority: 'HIGH',
        assigneeList: [OFFLINE_ASSIGNEES[1]!],
        createdDate: '2023-11-15',
        dueDate: '2023-12-10',
        finishedDate: ''
    },
    {
        id: 3,
        title: 'Klausuren vorbereiten',
        description: 'Lernplan erstellen',
        finished: true,
        priority: 'LOW',
        assigneeList: [],
        createdDate: '2023-10-01',
        dueDate: '2023-10-30',
        finishedDate: '2023-10-28'
    }
];
// --- END OFFLINE FALLBACK DATA ---

async function handleResponse<T>(response: Response): Promise<T> {

    if (!response.ok) {

        const errorText = await response.text();

        throw new Error(errorText || `API request failed with status ${response.status}`);

    }

    // Check if response has content type application/json before parsing

    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {

        return response.json() as Promise<T>;

    }

    // If no content or not JSON, return null or an empty object based on expected return type

    return {} as T; // Or handle as per requirement for non-JSON responses

}



// Global error/loading handling can be added here, e.g., using a Vuex store or a global event bus

// For now, it will throw errors that can be caught by the calling component.



// --- Assignee API Calls ---



export async function getAssignees(): Promise<Assignee[]> {

    try {

        const response = await fetch(`${API_BASE_URL}/assignees`);

        return await handleResponse<Assignee[]>(response);

    } catch (error) {

        console.error('Failed to fetch assignees:', error);

        // Fallback for GET requests

        return OFFLINE_ASSIGNEES;

    }

}



export async function getAssignee(id: number): Promise<Assignee> {

    try {

        const response = await fetch(`${API_BASE_URL}/assignees/${id}`);

        return await handleResponse<Assignee>(response);

    } catch (error) {

        console.error(`Failed to fetch assignee with id ${id}:`, error);

        const assignee = OFFLINE_ASSIGNEES.find(a => a.id === id);

        if (assignee === undefined) {

            throw new Error(`Assignee with id ${id} not found, even in offline data.`);

        }

        const foundAssignee: Assignee = assignee; // Explicitly assign to a typed variable

        return foundAssignee;

    }

}



export async function createAssignee(assignee: AssigneeCreateUpdate): Promise<Assignee> {

    try {

        const response = await fetch(`${API_BASE_URL}/assignees`, {

            method: 'POST',

            headers: {

                'Content-Type': 'application/json',

            },

            body: JSON.stringify(assignee),

        });

        return await handleResponse<Assignee>(response);

    } catch (error) {

        console.error('Failed to create assignee:', error);

        throw error;

    }

}



export async function updateAssignee(id: number, assignee: AssigneeCreateUpdate): Promise<Assignee> {

    try {

        const response = await fetch(`${API_BASE_URL}/assignees/${id}`, {

            method: 'PUT',

            headers: {

                'Content-Type': 'application/json',

            },

            body: JSON.stringify(assignee),

        });

        return await handleResponse<Assignee>(response);

    } catch (error) {

        console.error(`Failed to update assignee with id ${id}:`, error);

        throw error;

    }

}



export async function deleteAssignee(id: number): Promise<void> {

    try {

        const response = await fetch(`${API_BASE_URL}/assignees/${id}`, {

            method: 'DELETE',

        });

        // DELETE often returns no content, just check if response is ok

        if (!response.ok) {

            const errorText = await response.text();

            throw new Error(errorText || `API delete request failed with status ${response.status}`);

        }

    } catch (error) {

        console.error(`Failed to delete assignee with id ${id}:`, error);

        throw error;

    }

}



// --- Todo API Calls ---



export async function getTodos(): Promise<Todo[]> {

    try {

        const response = await fetch(`${API_BASE_URL}/todos`);

        return await handleResponse<Todo[]>(response);

    } catch (error) {

        console.error('Failed to fetch todos:', error);

        // Fallback for GET requests

        return OFFLINE_TODOS;

    }

}



export async function getTodo(id: number): Promise<Todo> {

    try {

        const response = await fetch(`${API_BASE_URL}/todos/${id}`);

        return await handleResponse<Todo>(response);

    } catch (error) {

        console.error(`Failed to fetch todo with id ${id}:`, error);

        const todo = OFFLINE_TODOS.find(t => t.id === id);

        if (todo === undefined) { // Explicitly check if not found

            throw new Error(`Todo with id ${id} not found, even in offline data.`);

        }

        const foundTodo: Todo = todo; // Explicitly assign to a typed variable

        return foundTodo;

    }

}



export async function createTodo(todo: TodoCreateUpdate): Promise<Todo> {

    try {

        const response = await fetch(`${API_BASE_URL}/todos`, {

            method: 'POST',

            headers: {

                'Content-Type': 'application/json',

            },

            body: JSON.stringify(todo),

        });

        return await handleResponse<Todo>(response);

    } catch (error) {

        console.error('Failed to create todo:', error);

        throw error;

    }

}



export async function updateTodo(id: number, todo: TodoCreateUpdate): Promise<Todo> {

    try {

        const response = await fetch(`${API_BASE_URL}/todos/${id}`, {

            method: 'PUT',

            headers: {

                'Content-Type': 'application/json',

            },

            body: JSON.stringify(todo),

        });

        return await handleResponse<Todo>(response);

    } catch (error) {

        console.error(`Failed to update todo with id ${id}:`, error);

        throw error;

    }

}







export async function deleteTodo(id: number): Promise<void> {



    try {



        const response = await fetch(`${API_BASE_URL}/todos/${id}`, {



            method: 'DELETE',



        });



        if (!response.ok) {



            const errorText = await response.text();



            throw new Error(errorText || `API delete request failed with status ${response.status}`);



        }



    } catch (error) {



        console.error(`Failed to delete todo with id ${id}:`, error);



        throw error;



    }



}







// --- CSV Download API Calls ---







export async function downloadTodosCsv(): Promise<Blob> {



    try {



        const response = await fetch(`${API_BASE_URL}/csv-downloads/todos`);



        if (!response.ok) {



            const errorText = await response.text();



            throw new Error(errorText || `CSV download failed with status ${response.status}`);



        }



        return await response.blob();



    } catch (error) {



        console.error('Failed to download todos CSV:', error);



        throw error;



    }



}


