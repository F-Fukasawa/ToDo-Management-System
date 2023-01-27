package com.dmm.task;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TasksForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class CreateController {
	@Autowired
	private TasksRepository tasksRepository;
	
	//投稿登録画面表示
	@GetMapping("/main/create/{date}")
	public String create(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") @PathVariable LocalDate date) {
				
		return "create";
	}
	
	//マッピング設定
	@PostMapping("/main/create")
	public String create(TasksForm tasksForm, @AuthenticationPrincipal AccountUserDetails user) {
		
		Tasks tasks = new Tasks();
		tasks.setName(user.getName());
		tasks.setTitle(tasksForm.getTitle());
		tasks.setText(tasksForm.getText());
		tasks.setDate(tasksForm.getDate());
		
		//データベースに保存
		tasksRepository.save(tasks);
		
		//カレンダー画面へリダイレクト
		return "redirect:/main";
	}
}
