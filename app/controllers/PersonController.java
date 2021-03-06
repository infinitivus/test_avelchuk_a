package controllers;

import models.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;

import service.IPersonService;

import javax.inject.Inject;

public class PersonController extends Controller {

    private final Form<PersonData> form;
    private final MessagesApi messagesApi;
    private final IPersonService service;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public PersonController(FormFactory formFactory, MessagesApi messagesApi, IPersonService service) {
        this.form = formFactory.form(PersonData.class);
        this.messagesApi = messagesApi;
        this.service = service;
    }

    // GET  Контроллер страницы с формой ввода емайла, пароля и кнопками
    public Result login(Http.Request request) {
        return ok(views.html.login.render(form, request, messagesApi.preferred(request)));
    }

    // POST  Контроллер обработки данных полученных с начальной страницы по нажатиб кнопки вход
    public Result checkPerson(Http.Request checkRequest) {
        final Form<PersonData> boundForm = form.bindFromRequest(checkRequest);
        if (errorInput(boundForm)) {
            return badRequest(views.html.login.render(boundForm, checkRequest, messagesApi.preferred(checkRequest)));
        } else {
            PersonData data = boundForm.get();
            Person per = service.checkAuthentication(data);
            if (!(per == null)) {
                return ok(Html.apply("<h3>Пользователь:" + per.getEmail() +
                        "<br>Авторизация: " + per.getAuthorisation() + "</h3>"
                        + "<br><button onclick=\"window.location.href = 'http://localhost:9000/';\">Выход</button>"));
            } else {
                return badRequest(views.html.login.render(boundForm, checkRequest, messagesApi.preferred(checkRequest)));
            }
        }
    }

    // POST  Контроллер обработки данных полученных с начальной страницы по нажатию кнопки регистрация
    public Result registrationPerson(Http.Request regRequest) {
        final Form<PersonData> boundForm = form.bindFromRequest(regRequest);
        if (errorInput(boundForm)) {
            return badRequest(views.html.login.render(boundForm, regRequest, messagesApi.preferred(regRequest)));
        } else {
            PersonData data = boundForm.get();
            if (service.registration(data)) {
                return ok(Html.apply("<h3>Пользователь зарегистрирован:" + data.getEmail() +
                        "<br>Авторизация: False <br>Для авторизации пройдите по ссылке на почте </h3>"
                        + "<br><button onclick=\"window.location.href = 'http://localhost:9000/';\">Выход</button>"));
            } else {
                return ok(Html.apply("<h3>Ошибка регистрации пользователя " + data.getEmail()
                        + "<br>Такой пользователь уже существует или невозможно отправить ссылку на авторизацию<h3>"
                        + "<br><button onclick=\"window.location.href = 'http://localhost:9000/';\">Выход</button>"));
            }
        }
    }

    //POST Контроллер обработки запроса авторизации
    public Result authorisationPerson(Http.Request authRequest) {
        final Form<PersonData> boundForm = form.bindFromRequest(authRequest);
        PersonData data = boundForm.get();
        Person per = service.authorisation(data);
        if (per != null) {
            return ok(Html.apply("<h3>Пользователь:" + per.getEmail() +
                    "<br>Авторизация:" + per.getAuthorisation() + "/h3>"
                    + "<button onclick=\"window.location.href = 'http://localhost:9000/';\">Выход</button>"));
        }
            return badRequest(views.html.login.render(boundForm, authRequest,
                    messagesApi.preferred(authRequest)));
    }

    // проверка на правильность ввода данных пользователем
    private boolean errorInput(Form<PersonData> form) {
        if (form.hasErrors()) {
            logger.error("errors = {}", form.errors());
            return true;
        }
        return false;
    }
}
