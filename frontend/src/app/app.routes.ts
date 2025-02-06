import {provideRouter, Routes} from '@angular/router';
import { HomePageComponent } from './shared/home-page/home-page.component';
import {AddProductComponent} from "./pages/add-product/add-product.component";
import { OrdersListComponent } from './order-list/order-list.component';
import { InventoryComponent } from './inventory/inventory.component';
import { CartComponent } from './cart/cart.component';
import { OrderDetailsComponent } from './order-details/order-details.component';
import { UserOrdersComponent } from './user-orders/user-orders.component';
import { PaymentComponent } from './payment/payment.component';
import { CategoryComponent } from './category/category.component';
import { UserInfoComponent } from './user-info/user-info.component';
import { UserDashboardComponent } from './user-dashboard/user-dashboard.component';
import { CategoryListComponent } from './categories-list/categories-list.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import {CategoryDetailsComponent} from "./category-details/category-details.component";
import {ContactUsComponent} from "./contact-us/contact-us.component";
import {AdminContactBoardComponent} from "./admin-contact-board/admin-contact-board.component";
import {EmailDetailsComponent} from "./email-details/email-details.component";
import {SidebarComponent} from "./sidebar/sidebar.component";

export const routes: Routes = [
    { path: '', redirectTo: '/products', pathMatch: 'full' },
    { path: 'user-dashboard', component: UserDashboardComponent, children: [
      { path: 'user-info', component: UserInfoComponent },
      { path: 'contact-panel', component: AdminContactBoardComponent },
        { path: 'user-orders', component: UserOrdersComponent },
      { path: '', redirectTo: 'user-info', pathMatch: 'full' }
    ]
    },
    { path: 'products', component: HomePageComponent },
    { path: 'sidebar', component: SidebarComponent },
    { path: 'orders', component: OrdersListComponent },
    { path: 'add-product', component: AddProductComponent },
    { path: 'inventory', component: InventoryComponent },
    { path: 'cart', component: CartComponent },
    { path: 'order-details/:id', component: OrderDetailsComponent },
    { path: 'payment', component: PaymentComponent },
    { path: 'categories', component: CategoryComponent },
    { path: 'categories-list', component: CategoryListComponent },
    { path: 'product-details/:id', component: ProductDetailsComponent },
    { path: 'category-details/:id', component: CategoryDetailsComponent },
    { path: 'contact-us', component: ContactUsComponent },
    { path: 'contact-panel', component: AdminContactBoardComponent },
    { path: 'email/:id', component: EmailDetailsComponent }
  ];
