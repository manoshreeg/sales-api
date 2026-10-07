package com.example.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {
    List<CategoryRule> findByActiveTrueOrderByPriorityAscIdAsc();
}
