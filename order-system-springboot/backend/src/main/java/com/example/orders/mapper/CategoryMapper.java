package com.example.orders.mapper;

import com.example.orders.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    int insertCategory(Category category);
    Category selectCategoryById(Long id);
    List<Category> selectAllCategories();
    List<Category> selectCategoriesByParentId(Long parentId); // 查询子分类
    int updateCategory(Category category);
    int deleteCategoryById(Long id);
}
