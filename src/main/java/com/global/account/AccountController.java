package com.global.account;

import com.global.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

  private final SignUpFormValidator signUpFormValidator;
  private final AccountRepository accountRepository;
  private final JavaMailSender javaMailSender;

  @InitBinder("signUpForm")
  public void initBinder(WebDataBinder webDataBinder){
    webDataBinder.addValidators(signUpFormValidator);

  }

  // localhost:8080/sign-up 을 주소표시줄에 입력했을 때
  // 자동으로 호출되는 메소드
  @GetMapping("/sign-up")
  public String signUpForm(Model model){
    model.addAttribute("signUpForm", new SignUpForm());
    return "account/sign-up";
  }

  // 회원가입 페이지에서 submit 버튼 눌렀을 때
  // 자동으로 호출되는 메소드
  @PostMapping("/sign-up")
  public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
    if(errors.hasErrors()){
      // 에러가 발생하면 다음 페이지로 넘어가지 않고
      // 다시 sign-up 페이지를 보여줌
      return "account/sign-up";
    }

    /*
    위에 작성한
    public void initBinder(WebDataBinder webDataBinder) 메소드에서
    검증 작업을 진행함

   ormValidator.validate(signUpForm, errors);
    if(errors.hasErrors()){
      // 에러가 발생하면 다음 페이지로 넘어가지 않고
      // 다시 sign-up 페이지를 보여줌
      return "account/sign-up";
    }
    */

    Account account = Account.builder()
                             .email(signUpForm.getEmail())
                             .nickName(signUpForm.getNickname())
                             .password(signUpForm.getPassword())
                             .studyCreateByWeb(true)
                             .studyEnrollmentResultByWeb(true)
                             .studyUpdateByWeb(true)
                             .build();

    Account newAccount = accountRepository.save(account);

    // 이메일 보내기 전에 토큰값 생성하기
    newAccount.generateEmailCheckToken();

    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    // 토큰값에 해당하는 이메일 주소 받기
    simpleMailMessage.setTo(newAccount.getEmail());
    // 이메일 제목
    simpleMailMessage.setSubject("회원 가입 인증");
    // 이메일 본문
    // simpleMailMessage.setText("/check-email-token?token=이메일보내기전에생성한토큰값&email=토큰값에해당하는이메일주소");
    simpleMailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
                                                          + "&email=" + newAccount.getEmail());
    javaMailSender.send(simpleMailMessage);

    // 회원가입 폼이 제대로 입력된 경우,
    // Home(/) 으로 이동함
    return "redirect:/";
  }
}
