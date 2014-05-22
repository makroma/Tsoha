package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.User
import models.Movie
import models.Genre
import views._

object Auth extends Controller {


	/*
	Fronpage renderer, "index = action" opens frontpage, with movies list and nav bar login form

	!Now only authenticate
	*/

	def index = Action { implicit request =>
		var movies = Movie.findAll
		var genres = Genre.allSorted
		Ok(html.movies.list(movies)(genres)(null))
  	}

  	/*
	Log in form, with "tuple-match-case"
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

	/*
	Login form authenticate. 
	BUG: if not valid, used username and password goes to addressbar
	*/

    def authenticate = Action { implicit request =>
    	var movies = Movie.findAll
    	var genres = Genre.allSorted
    	val newLoginForm = loginForm.bindFromRequest()
    	newLoginForm.fold(
    		hasErrors = { formWithErrors => 
    			BadRequest(html.movies.list(movies)(genres)(views.html.admin.login(formWithErrors))).flashing(
            "error" -> "Unable to login")},
    		success = { user => 
   
    			Redirect(routes.Admin.dashboard).withSession(Security.username -> user._1)
    			
  			}
  		)	
  	}



    def logout = Action {
    	Redirect(routes.Auth.authenticate).withNewSession.flashing(
      	"success" -> "You are now logged out. Thank you for your support!")
  	}
}
