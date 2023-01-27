package com.dmm.task;

import org.springframework.beans.factory.annotation.Autowired;
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
public class EditController {
	@Autowired
	private TasksRepository tasksRepository;
	
	//編集画面表示
	@GetMapping("/main/edit/{id}")
	public String edit(Model model, @PathVariable Long id) {
		Tasks task = getById(id);
		model.addAttribute("task", task);
		
		return "edit";
	}
	
	//idでTasksを検索
	@GetMapping("{id}")
		public Tasks getById(@PathVariable Long id) {
		//IDで検索
		return tasksRepository.findById(id).orElseThrow();
	}
	
	//マッピング設定
	@PostMapping("/main/edit/{id}")
	public String edit(@PathVariable Long id, TasksForm tasksForm, @AuthenticationPrincipal AccountUserDetails user) {
		
		Tasks tasks = getById(id);
		tasks.setName(user.getName());
		tasks.setTitle(tasksForm.getTitle());
		tasks.setText(tasksForm.getText());
		tasks.setDate(tasksForm.getDate());
		tasks.setDone(tasksForm.isDone());
		
		//データベースに保存
		tasksRepository.save(tasks);
		
		//カレンダー画面へリダイレクト
		return "redirect:/main";
	}
	
	//delete処理
	@PostMapping("/main/delete/{id}")
	public String deleteEdit(@PathVariable Long id) {
		tasksRepository.deleteById(id);
		return "redirect:/main";
	}
}
