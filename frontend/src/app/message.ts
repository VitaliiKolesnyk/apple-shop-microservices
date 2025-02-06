export interface Message {
  id: number;
  subject: string;
  email: string;
  status: string;
  timestamp: Date,
  lastReplyFrom: string;
  lastReplyAt: Date;

}

