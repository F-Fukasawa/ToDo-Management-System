package com.dmm.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.data.repository.UsersRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
//@RequestMapping("/main")
public class MainController {
	@Autowired
	private TasksRepository tasksRepository;
	
	@Autowired
	private UsersRepository usersRepository;
	
	//カレンダー表示
	@GetMapping("/main")
	public String getMain(Model model, @AuthenticationPrincipal AccountUserDetails user) {

		/*初期処理*/
		//当月の初日を取得
		LocalDate firstDate = LocalDate.now().withDayOfMonth(1);		
		
		/*年月の表示*/
		YearMonth yearmonth = YearMonth.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
		model.addAttribute("month", dateTimeFormatter.format(yearmonth));
		
		
		/*タスク取得*/
		LinkedHashMap<LocalDate, List<Tasks>> tasks = new LinkedHashMap<LocalDate, List<Tasks>>();
		
		//当月の最終日をを取得
        LocalDate lastDate = firstDate.with(TemporalAdjusters.lastDayOfMonth());
        
        String userRole = usersRepository.findByUser(user.getName());
        List<Tasks> task = new ArrayList<>();
        //管理者の場合、全ユーザーのタスクを取得
        if (("ADMIN").equals(userRole)) {
        	task = tasksRepository.findByDateBetweenAll(firstDate, lastDate);
        } else {
        	task = tasksRepository.findByDateBetween(firstDate, lastDate, user.getName());
        }
        
        Collections.sort(task);
        List<Tasks> taskDay = new ArrayList<>();
        LocalDate date1 = firstDate;
        for (int i = 0; i < task.size(); i++) {
        	if (date1.isEqual(task.get(i).getDate())) {
        		taskDay.add(task.get(i));
        	} else {
        		tasks.put(date1, taskDay);
        		taskDay = new ArrayList<>();
        		taskDay.add(task.get(i));
        		date1 = task.get(i).getDate();
        	}
        }
		
		model.addAttribute("tasks", tasks);
		
		
		/*カレンダーの取得*/
		//Listのネスト
		List<List<LocalDate>> month = new ArrayList<>();
		
		//1週間分のLocalDateを格納するList
		List<LocalDate> week = new ArrayList<>();
								
		//当週の前月分を取得
		int beforeMonthDate = 7 - (firstDate.getDayOfWeek().getValue());
		LocalDate date2 = firstDate;
		//当週の前月分をweekリストに追加
		for (int i = 0; i < beforeMonthDate; i++) {
			week.add(date2);
			date2 = date2.plusDays(1);
		}
		
		// 当月の最後の日を取得
		int lastDay = date2.lengthOfMonth();
		//当月の1週目からweekリストに追加
		for (int i = 1; i <= lastDay; i++) {
			week.add(date2);
			
			//土曜日になったらmonthリストへ追加
			DayOfWeek dayOfWeek = date2.getDayOfWeek();
			if (dayOfWeek == DayOfWeek.SATURDAY) {
				month.add(week);
				week = new ArrayList<>();
			}
			date2 = date2.plusDays(1);
		}
		
		//最終週の翌月分を取得
		int afterMonthDate = 7 - (date2.getDayOfWeek().getValue());
		//最終週の翌月分をweekリストに追加
		for (int i = 0; i < afterMonthDate; i++) {
			week.add(date2);
			date2 = date2.plusDays(1);
		}		
		month.add(week);
		
		model.addAttribute("matrix", month);
		
		
		
		/*前月へ*/
		LocalDate before = LocalDate.now().withDayOfMonth(1).minusDays(1);
		before = before.withDayOfMonth(1);
		model.addAttribute("prev", before);
		

		return "main";
	}
}
