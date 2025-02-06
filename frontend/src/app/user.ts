export interface User {
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    attributes: Attribute;
}

export interface Attribute {
    receiveNotification: string
}