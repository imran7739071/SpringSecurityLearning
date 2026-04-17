import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar/navbar.component';
import { ForbiddenComponent } from './forbidden/forbidden.component';



@NgModule({
  declarations: [
    NavbarComponent,
    ForbiddenComponent
  ],
  imports: [
    CommonModule
  ]
})
export class SharedModule { }
