package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Security
import play.api.mvc.Results._

import scala.concurrent._
import play.api.mvc.Results._

import models.User
import models.Movie
import models.Genre
import views._

object Auth extends Controller with Secured{

  /*
  Login form, with "tuple-match-case"
  */

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text).verifying ("Invalid username or password", result => 
        result match {
          case (username, password) => check(username, password)
        }
      )
  )

  def check(username: String, password: String): Boolean = {
    User.checkUserPassword(username, password)
  }

  /**
  Login form authenticate. 
  */

  def login = Action { implicit request =>
    Ok(html.auth.login(loginForm))
  }

  /**
  login form authentication. Sets Session if Ok
  */

  def authenticate = Action { implicit request =>

    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.auth.login(formWithErrors)),
      user => Redirect(routes.Auth.userDirect).withSession("username" -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Auth.authenticate).withNewSession.flashing(
      "success" -> "You are now logged out. Thank you for your support!")
  }

  /**
  * User Direction based on roles not implemented yet
  */

  def userDirect = withUser { user => implicit request =>
    val username = user.username
    val admin = user.admin
    println("userDirect: " + username + " is admin: " + admin)
    
    if(admin) Redirect(routes.Admin.dashboard).flashing(
      "success" -> "Welcome! You are now logged with adminstrator priviledges")
    else Redirect(routes.Movies.frontPage).flashing(
      "success" -> "Welcome! You are now logged in as a user")
  }
}  

/**
 * Provide security features
 * http://www.playframework.com/documentation/2.0.1/ScalaSecurity
 */

trait Secured {
  
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.authenticate)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      val u = User.findByName(user).getOrElse(null)
      Action(request => f(user)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    User.findByName(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }

  /*
  * Admin check. if not admin rights, will direct to fronpage with flashing
  */

  def withAdmin(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    val optionUser = User.findByName(username)
    val user = optionUser.getOrElse(null)

    user.admin match {
      case true => {
        optionUser.map { user =>
          f(user)(request)
        }.getOrElse(onUnauthorized(request))
      }
      case _ => Results.Redirect(routes.Movies.frontPage).flashing(
      "error" -> "Sorry, You're Not Allowed to Do That ")
    }
  }
}
