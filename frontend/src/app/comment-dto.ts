export interface CommentDto {
  id: string;
  author: string;
  text: string;
  createdAt: string;
  replies: CommentDto[];
  showReplies: false;
}
