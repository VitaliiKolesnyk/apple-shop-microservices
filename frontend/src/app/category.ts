export interface Category {
  id: string;
  name: string;
  isMain: boolean,
  thumbnailUrl: string;
  parentCategoryId?: string;
  subcategories: Subcategory[];
}

export interface Subcategory  {
  id: string;
  name: string;
  subcategories?: Subcategory[];
}
