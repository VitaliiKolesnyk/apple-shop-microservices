import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminContactBoardComponent } from './admin-contact-board.component';

describe('AdminContactBoardComponent', () => {
  let component: AdminContactBoardComponent;
  let fixture: ComponentFixture<AdminContactBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminContactBoardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminContactBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
