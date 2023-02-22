package tech.araopj.springpitzzahhbot.config.category.service;

import org.springframework.beans.factory.annotation.Autowired;
import tech.araopj.springpitzzahhbot.config.category.CategoryConfig;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryConfig categoryConfig;

    @Autowired
    public CategoryService(CategoryConfig categoryConfig) {
        this.categoryConfig = categoryConfig;
    }

    public String welcomeCategoryName() {
        return categoryConfig.getWelcomeCategory();
    }

    public String updatesCategoryName() {
        return categoryConfig.getMemberUpdatesCategory();
    }

    public String secretsCategoryName() {
        return categoryConfig.getCreateConfessionsCategory();
    }
}
