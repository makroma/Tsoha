package controllers

import play.api._
import play.api.mvc._

import views._

object Application extends Controller with Secured{


  /*
  Fronpage renderer, "index = action" opens frontpage, with movies list
  */

  def index = Action { implicit request =>
   Redirect(routes.Movies.frontPage)
  }

  /*
  Error renderer, "index = action" opens frontpage, with movies list
  */

  def error(message:String = "not defined") = Action { implicit request =>
    Ok(html.error(message))
  }
}