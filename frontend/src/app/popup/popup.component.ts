// popup.component.ts
import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-popup',
  templateUrl: './popup.component.html',
  styleUrls: ['./popup.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class PopupComponent {
  @Input() title: string = 'Notification';
  @Input() message: string = 'Default message';
  @Output() closed = new EventEmitter<void>();
  
  isActive = false; // Track the active state of the popup

  ngOnInit() {
    this.isActive = true; // Set the popup to active when initialized
  }

  closePopup() {
    this.isActive = false; // Set to inactive when closing
    this.closed.emit();
    // Optionally emit an event to inform the parent component
  }
}
