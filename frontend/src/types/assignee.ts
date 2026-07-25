export interface Assignee {
    id: number;
    prename: string;
    name: string;
    email: string;
}

export interface AssigneeCreateUpdate {
    prename: string;
    name: string;
    email: string;
}
