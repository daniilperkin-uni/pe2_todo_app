import type { Assignee } from './assignee'; // Add this line

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Todo {
    id: number;
    title: string;
    description: string;
    finished: boolean;
    priority: Priority;
    assigneeList: Assignee[];
    createdDate: string; // ISO-8601 date string (yyyy-MM-dd)
    dueDate: string;     // ISO-8601 date string (yyyy-MM-dd)
    finishedDate: string; // ISO-8601 date string (yyyy-MM-dd)
    category?: string | null;
}

export interface TodoCreateUpdate {
    title: string;
    description: string;
    finished: boolean;
    priority: Priority;
    dueDate: string; // ISO-8601 date string (yyyy-MM-dd)
    assigneeIdList: number[]; // List of Assignee IDs
}
