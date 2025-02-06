export interface MessageDto {
  id: number;
  subject: string;
  body: string;
  email: string;
  status: string;
  timestamp: Date
  replies: Reply[],
}

export interface Reply {
  body: string;
  timestamp: Date;
  emailStatus: string;
}
