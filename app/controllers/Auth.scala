package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Security

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
      "password" -> text) verifying ("Invalid username or password", result => result match {
        	case (username, password) => check(username, password)
  		})
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

  def userDirect = withUser{ user => implicit request =>
    val username = user.username
    val admin = user.admin
    println("userDirect: " + username + " is admin: " + admin)

    if(admin) Redirect(routes.Admin.dashboard).withSession("username" -> username)
    else Redirect(routes.Application.index).withSession("username" -> username).flashing(
      "success" -> "You are now logged is as user")
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
      //Authenticated 
      val u = User.findByName(user).getOrElse(null)
      
      if(u.admin) Action(request => f(user)(request))
      else Action(request => f(user)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    User.findByName(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}
