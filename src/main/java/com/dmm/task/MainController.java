package com.dmm.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.data.repository.UsersRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {
	@Autowired
	private TasksRepository tasksRepository;
	
	@Autowired
	private UsersRepository usersRepository;
	
	//カレンダー表示
	@GetMapping("/main")
	public String getMain(Model model, @AuthenticationPrincipal AccountUserDetails user,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		
		//1日を取得
		LocalDate firstDay = getFirstDay(date);
		
		//最終日をを取得
        LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());
		
		//前月表示
        LocalDate beforeMonthFirstDate = getLastMonthDay(firstDay);
		model.addAttribute("prev", beforeMonthFirstDate);
		
		//翌月表示
		LocalDate nextMonthFirstDate = getNextMonthDay(lastDay);
		model.addAttribute("next", nextMonthFirstDate);
		
		//年月の表示
		model.addAttribute("month", firstDay.getYear() + "年" + firstDay.getMonthValue() + "月");
		
		//タスク取得
		//タスク用のMap
		LinkedHashMap<LocalDate, List<Tasks>> monthTasks = new LinkedHashMap<LocalDate, List<Tasks>>();
		
		//Usersからuser_nameを取得
		String userRole = usersRepository.findByUser(user.getName());
		List<Tasks> tasks = new ArrayList<>();
		//該当ユーザーのタスクを取得（管理者の場合、全ユーザーのタスクを取得）
		if (("ADMIN").equals(userRole)) {
			tasks = tasksRepository.findByDateBetweenAll(firstDay, lastDay);
		} else {
			tasks = tasksRepository.findByDateBetween(firstDay, lastDay, user.getName());
		}
		
		//日付順に並び替え
		Collections.sort(tasks);
		
		//日毎のタスクの集計
		List<Tasks> dayTasks = new ArrayList<>();
		LocalDate beforeTaskDate = firstDay;
		LocalDate nowTaskDate = firstDay;
		for (int i = 0; i < tasks.size(); i++) {
			nowTaskDate = tasks.get(i).getDate();
			if (i > 0 && !beforeTaskDate.isEqual(nowTaskDate)) {
				monthTasks.put(beforeTaskDate, dayTasks);
				dayTasks = new ArrayList<>();
			}
			dayTasks.add(tasks.get(i));
			beforeTaskDate = nowTaskDate;
		}
		monthTasks.put(nowTaskDate, dayTasks);
		
		model.addAttribute("tasks", monthTasks);
		
		//カレンダーの表示
		//月のリスト
		List<List<LocalDate>> month = new ArrayList<>();
		
		//1週間分のLocalDateを格納するリスト
		List<LocalDate> week = new ArrayList<>();
		
		//当週の前月分を取得
		int beforeMonthDayValue = firstDay.getDayOfWeek().getValue();
		if (beforeMonthDayValue != 7) {
			LocalDate beforeMonthDay = firstDay.minusDays(beforeMonthDayValue);
			//当週の前月分をweekリストに追加
			for (int i = 0; i < beforeMonthDayValue; i++) {
				week.add(beforeMonthDay);
				beforeMonthDay = beforeMonthDay.plusDays(1);
			}
		}
		
		// 当月の最後の日を取得
		int lastDayValue = firstDay.lengthOfMonth();
		LocalDate currentMonthDate = firstDay;
		//当月の1週目からweekリストに追加
		for (int i = 1; i <= lastDayValue; i++) {
			week.add(currentMonthDate);
			
			//土曜日になったらmonthリストへ追加
			DayOfWeek dayOfWeek = currentMonthDate.getDayOfWeek();
			if (dayOfWeek == DayOfWeek.SATURDAY) {
				month.add(week);
				week = new ArrayList<>();
			}
			currentMonthDate = currentMonthDate.plusDays(1);
		}
		
		//最終週の翌月分を取得
		int afterMonthDate = 7 - (currentMonthDate.getDayOfWeek().getValue());	//この時点でcurrentMonthDateは末日になっている
		//最終週の翌月分をweekリストに追加
		for (int i = 0; i < afterMonthDate; i++) {
			week.add(currentMonthDate);
			currentMonthDate = currentMonthDate.plusDays(1);
		}		
		month.add(week);
		
		model.addAttribute("matrix", month);
		
		return "main";
	}
	
	/*
	 * 1日の取得
	 */
	public LocalDate getFirstDay(LocalDate parmDate) {
		if (parmDate == null) {
			parmDate = LocalDate.now().withDayOfMonth(1);
		}
		return parmDate; 
	}
	
	/*
	 *前月日取得
	 *
	 * @parm monthFirstDay 当月の1日
	 * @return lastMonthDay 前月の1日
	 */
	public LocalDate getLastMonthDay(LocalDate monthFirstDay) {
		LocalDate lastMonthDay = monthFirstDay.minusDays(1);
		lastMonthDay = lastMonthDay.withDayOfMonth(1);
		
		return lastMonthDay; 
	}
	
	
	/*
	 *翌月日取得
	 *
	 * @parm monthLastDay 当月の最終日
	 * @return nextMonthDay 翌月の1日
	 */
	public LocalDate getNextMonthDay(LocalDate monthLastDay) {
		LocalDate nextMonthDay = monthLastDay.plusDays(1);
		
		return nextMonthDay; 
	}
}
