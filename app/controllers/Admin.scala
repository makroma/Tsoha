package controllers

import play.api.mvc.{Action, Controller}
import models.User

object Admin extends Controller {

  def list = Action { implicit request =>
    val users = User.findAllSQL
    Ok(views.html.main("Admin Panel")(views.html.admin.users(users)))
  }
}