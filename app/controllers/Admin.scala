package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Flash
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms.{mapping, number, nonEmptyText, boolean, tuple, text}
import play.api.mvc.{Action, Controller}

import models.User
import models.Aspirant
import models.Genre

object Admin extends Controller {

  /*
  Dashboard renderer @(content: Html)(right: Html)(implicit flash: Flash)
  */

	def dashboard = Action { implicit request =>
		val users = User.findAllSQL
		Ok(views.html.admin.dashboard(null)(views.html.admin.users(users)))
	}

  /*
	Renders userpage or return 404
  camelUser(selectors)(where)(is)
	*/  

  def showUser(id: Int) = Action { implicit request =>
   // User.findCamelUser("*")("userid")(id.toString).map 
      User.findById(id).map {user =>
	  		Ok(views.html.admin.user(user))
		}.getOrElse(NotFound)
  }

  /*
  Userform for adding new user
  Aspirant -type have only username and password
  */

  val userForm: Form[Aspirant] = Form(
    mapping(
      "username" -> nonEmptyText.verifying(
        "Not unique username", User.findByName(_).isEmpty),
      "password" -> tuple(
          "main" -> nonEmptyText,
          "confirm" -> nonEmptyText).verifying( 
            "Passwords don't match", password => password._1 == password._2 
          ).transform(
            { case (main, confirm) => main },
            ( main: String) => ("", "")
        )
    )(Aspirant.apply)(Aspirant.unapply)
  )

  /*
  Add new user function. BindFromRequest method searches the request 
  parameters for ones named after the form’s fields and uses them as 
  those fields values. Validation happens at binding. 
  If errors, redirects to add page. 
  if success, adds user to db. Db adds unique Id to user, which in this
  point is '-1'. Find same user by name and redirect to userpage.
  */

  def adduser = Action { implicit request =>
    print("addUser ")
    val newUserForm = userForm.bindFromRequest()
    newUserForm.fold(
      hasErrors = { form =>
        //println("has errors." + form)
        BadRequest(views.html.admin.addUser(form)).flashing(
        //Redirect(routes.Admin.newUser()).flashing(//Flash(form.data) +
            "error" -> "Something went wrong, maybe username is taken or passwords didnt match ")},
      success = { user =>
        println("success.")
        User.addUser(user.username, user.userpassword)
        Redirect(routes.Admin.showUser(User.idByName(user.username)))
      } 
    )
  }

  def newUser = Action { implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
      println("error, flashing data: " + flash.data)
        userForm.bind(flash.data)
        //flash
    } else userForm

    Ok(views.html.admin.addUser(form))
  }

  /*
  Genre form : add and delete functions
  */

  val genreForm: Form[Genre] = Form(
    mapping(
      "genre" -> text.verifying(
        "Not unique genre", Genre.findByGenre(_).isEmpty)
    )(Genre.apply)(Genre.unapply)
  )

    def addgenre = Action { implicit request =>
    val newGenreForm = genreForm.bindFromRequest()
    newGenreForm.fold(
      hasErrors = { form =>
        println("genre has errors")
        BadRequest(views.html.admin.editGenre(form)(views.html.admin.genres(Genre.allSorted))).flashing(
            "error" -> "Something went wrong, maybe name taken")},
      success = { genre =>
        println("success.")
        Genre.addGenre(genre.title)
        Ok(views.html.admin.editGenre(genreForm)(views.html.admin.genres(Genre.allSorted))).flashing(
            "success" -> "New genre added")
      } 
    )
  }

  def showGenres = Action { implicit request =>
    val form = 
    if (flash.get("error").isDefined) {
      println("error, flashing data: " + flash.data)
        genreForm.bind(flash.data)
        //flash
    } else genreForm

    Ok(views.html.admin.editGenre(form)(views.html.admin.genres(Genre.allSorted)))
  }

  def deleteGenre(title: String) = Action {
    Genre.delete(title)
    Redirect(routes.Admin.showGenres)
}





}