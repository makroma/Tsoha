package controllers

import play.api._
import play.api.mvc._

import models.Movie
import models.Genre
import models.User
import views._

object Application extends Controller with Secured{


  /*
  Fronpage renderer, "index = action" opens frontpage, with movies list
  */

  def index = Action { implicit request =>
   Redirect(routes.Movies.frontPage)
  
  }
  def error(message:String = "not defined") = Action { implicit request =>

    Ok(html.error(message))

  }
}