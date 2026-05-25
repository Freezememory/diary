package com.diary.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diary.common.core.domain.R;
import com.diary.core.entity.DiaryContent;
import com.diary.core.entity.DiaryImage;
import com.diary.core.entity.DiaryItem;
import com.diary.core.service.DiaryContentService;
import com.diary.core.service.DiaryImageService;
import com.diary.core.service.DiaryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/diary/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final DiaryContentService contentService;
    private final DiaryImageService imageService;
    private final DiaryItemService itemService;

    @GetMapping
    public R<List<LocalDate>> getDiaryDates(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam int year,
                                            @RequestParam int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Set<LocalDate> allDates = new HashSet<>();

        // 查询有文字日记的日期
        List<DiaryContent> contents = contentService.list(new LambdaQueryWrapper<DiaryContent>()
                .eq(DiaryContent::getUserId, userId)
                .between(DiaryContent::getDiaryDate, startDate, endDate)
                .select(DiaryContent::getDiaryDate));
        contents.forEach(c -> allDates.add(c.getDiaryDate()));

        // 查询有图片的日期
        List<DiaryImage> images = imageService.list(new LambdaQueryWrapper<DiaryImage>()
                .eq(DiaryImage::getUserId, userId)
                .between(DiaryImage::getDiaryDate, startDate, endDate)
                .select(DiaryImage::getDiaryDate));
        images.forEach(i -> allDates.add(i.getDiaryDate()));

        // 查询有清单条目的日期
        List<DiaryItem> items = itemService.list(new LambdaQueryWrapper<DiaryItem>()
                .eq(DiaryItem::getUserId, userId)
                .between(DiaryItem::getDiaryDate, startDate, endDate)
                .select(DiaryItem::getDiaryDate));
        items.forEach(i -> allDates.add(i.getDiaryDate()));

        return R.ok(new ArrayList<>(allDates));
    }
}
