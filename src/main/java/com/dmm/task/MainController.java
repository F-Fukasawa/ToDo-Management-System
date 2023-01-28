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
		LocalDate firstDate = getFirstDay(date);
		
		//最終日をを取得
        LocalDate lastDate = firstDate.with(TemporalAdjusters.lastDayOfMonth());
		
		//前月表示
        LocalDate beforeMonthFirstDate = getLastMonthDay(firstDate);
		model.addAttribute("prev", beforeMonthFirstDate);
		
		//翌月表示
		LocalDate nextMonthFirstDate = getNextMonthDay(lastDate);
		model.addAttribute("next", nextMonthFirstDate);
		
		//年月の表示
		model.addAttribute("month", firstDate.getYear() + "年" + firstDate.getMonthValue() + "月");
		
		//タスクを取得
		List<Tasks> tasks = getTasks(firstDate, lastDate, user.getName());
		
		//日付順に並び替え
		Collections.sort(tasks);
		
		//タスクをセット
		LinkedHashMap<LocalDate, List<Tasks>> monthTasks = new LinkedHashMap<LocalDate, List<Tasks>>();
		monthTasks = getMonthTasks(firstDate, tasks, monthTasks);
		model.addAttribute("tasks", monthTasks);
		
		//カレンダーの表示
		List<List<LocalDate>> month = new ArrayList<>();
		month = getBeforeMonthDate(firstDate, month);
		
		model.addAttribute("matrix", month);
		
		return "/main";
	}
	
	
	/*
	 * 1日の取得
	 * @parm   parmDate URLパラメーターのdate
	 * @return parmDate その月の1日
	 */
	public LocalDate getFirstDay(LocalDate parmDate) {
		if (parmDate == null) {
			parmDate = LocalDate.now().withDayOfMonth(1);
		}
		return parmDate; 
	}
	
	/*
	 *前月日取得
	 * @parm   monthFirstDay 当月の1日
	 * @return lastMonthDay  前月の1日
	 */
	public LocalDate getLastMonthDay(LocalDate monthFirstDay) {
		LocalDate lastMonthDay = monthFirstDay.minusDays(1);
		lastMonthDay = lastMonthDay.withDayOfMonth(1);
		
		return lastMonthDay; 
	}
		
	/*
	 *翌月日取得
	 * @parm   monthLastDay 当月の末日
	 * @return nextMonthDay 翌月の1日
	 */
	public LocalDate getNextMonthDay(LocalDate monthLastDay) {
		LocalDate nextMonthDay = monthLastDay.plusDays(1);
		
		return nextMonthDay; 
	}
	
	/*
	 * タスクを取得
	 * @parm   firstDate 当月の1日
	 * @parm   lastDate  当月の末日
	 * @parm   name      ログインユーザーのuserName
	 * @return tasks     検索したタスクのリスト
	 */
	public List<Tasks> getTasks(LocalDate firstDate, LocalDate lastDate, String name) {
		//ログインユーザーのuserNameを取得
		String userRole = usersRepository.findByUser(name);
		
		List<Tasks> tasks = new ArrayList<>();
		
		//当週の前月分の最初の日付を取得
		int beforeMonthDayValue = firstDate.getDayOfWeek().getValue();
		LocalDate fromDate = firstDate;
		if (beforeMonthDayValue != 7) {
			fromDate = getBeforeMonthDate(firstDate, beforeMonthDayValue);
		}
		
		//最終週の翌月分の最後の日付を取得
		int nextMonthDayValue = Math.abs((lastDate.getDayOfWeek().getValue()) -6);
		LocalDate toDate = getNextMonthDate(lastDate, nextMonthDayValue);
		
		//該当ユーザーのタスクを取得（管理者の場合、全ユーザーのタスクを取得）
		if (("ADMIN").equals(userRole)) {
			tasks = tasksRepository.findByDateBetweenAll(fromDate, toDate);
		} else {
			tasks = tasksRepository.findByDateBetween(fromDate, toDate, name);
		}
		
		return tasks;
	}
	
	/*
	 * 日毎のタスクの集計、Mapへのセット
	 * @parm   firstDate  当月の1日
	 * @parm   tasks      取得したタスク
	 * @parm   monthTasks カレンダー表示用のMap
	 * @return monthTasks カレンダー表示用のMap
	 */
	public LinkedHashMap<LocalDate, List<Tasks>> getMonthTasks(LocalDate firstDate, List<Tasks> tasks,
			LinkedHashMap<LocalDate, List<Tasks>> monthTasks){
				
		//日毎のタスク用リスト
		List<Tasks> dayTasks = new ArrayList<>();
		
		LocalDate beforeTaskDate = firstDate;
		LocalDate nowTaskDate = firstDate;
		for (int i = 0; i < tasks.size(); i++) {
			nowTaskDate = tasks.get(i).getDate();
			//リストの日付が異なる場合、dayTasksにセット
			if (i > 0 && !beforeTaskDate.isEqual(nowTaskDate)) {
				monthTasks.put(beforeTaskDate, dayTasks);
				dayTasks = new ArrayList<>();
			}
			dayTasks.add(tasks.get(i));
			beforeTaskDate = nowTaskDate;
		}
		monthTasks.put(nowTaskDate, dayTasks);
		
		return monthTasks;
	}
	
	/*
	 * LocalDateをカレンダー表示用Mapにセット
	 * @parm   firstDate 当月の1日
	 * @parm   month     カレンダー用Map
	 * @return month     カレンダー用Map
	 */
	public List<List<LocalDate>> getBeforeMonthDate(LocalDate firstDate, List<List<LocalDate>> month){

		//1週間分のLocalDateを格納するリスト
		List<LocalDate> week = new ArrayList<>();
		
		//当週の前月分を取得
		int beforeMonthDayValue = firstDate.getDayOfWeek().getValue();
		if (beforeMonthDayValue != 7) {
			LocalDate beforeMonthDay = getBeforeMonthDate(firstDate, beforeMonthDayValue);
			//当週の前月分をweekリストに追加
			for (int i = 0; i < beforeMonthDayValue; i++) {
				week.add(beforeMonthDay);
				beforeMonthDay = beforeMonthDay.plusDays(1);
			}
		}
		
		// 当月の最後の日を取得
		int lastDayValue = firstDate.lengthOfMonth();
		
		LocalDate currentMonthDate = firstDate;
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
		int nextMonthDayValue = 7 - (currentMonthDate.getDayOfWeek().getValue());	//この時点でcurrentMonthDateは翌月の1日になっている
		//最終週の翌月分をweekリストに追加
		for (int i = 0; i < nextMonthDayValue; i++) {
			week.add(currentMonthDate);
			currentMonthDate = currentMonthDate.plusDays(1);
		}		
		month.add(week);
		
		return month;
	}
	
	/*
	 * 当週の前月分の最初の日付を取得
	 * @parm   firstDate           当月の1日
	 * @parm   beforeMonthDayValue 当週の前月分の数
	 * @return beforeMonthDate     当週の前月分の最初の日付
	 */
	public LocalDate getBeforeMonthDate(LocalDate firstDate, int beforeMonthDayValue) {
		LocalDate beforeMonthDate = firstDate.minusDays(beforeMonthDayValue);
		return beforeMonthDate;
	}
	
	/*
	 * 最終週の翌月分の最後の日付を取得
	 * @parm   firstDate           当月の1日
	 * @parm   nextMonthDayValue 当週の翌日分の数
	 * @return nextMonthDate     当週の翌月分の最後の日付
	 */
	public LocalDate getNextMonthDate(LocalDate lastDate, int nextMonthDayValue) {
		LocalDate nextMonthDate = lastDate.plusDays(nextMonthDayValue);
		return nextMonthDate;
	}
}
