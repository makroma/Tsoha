package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Flash
import play.api.data._
import play.api.data.Form._
import play.api.data.Forms.{mapping, number, nonEmptyText, boolean, tuple, text, seq, list, optional, ignored}
import play.api.mvc.{Action, Controller}

import anorm._
import anorm.SqlParser._

import models._

object Userpage extends Controller with Secured{

  /*
  * Renders moviepage or return 404
  */  

  def showUserPage(usern: String) = withUser{ user => implicit request =>

    val authUser = Auth.username(request).getOrElse(null)
    
    if(usern == authUser) {
      val user:User = User.findByName(authUser).getOrElse(null)

      val form = {
        if (flash.get("error").isDefined) userForm.bind(flash.data)
        else userForm.fill(new Password(user.userpassword))
      }

      Ok(views.html.user.page(user, form))
    } 
    else NotFound
  }

  /*
  Userform for editing existing user
  */

  val userForm:Form[Password] = Form(
    mapping(
      "password" -> tuple(
        "main" -> nonEmptyText,
        "confirm" -> nonEmptyText).verifying( 
          "Passwords don't match", password => password._1 == password._2 
        ).transform(
          { case (main, confirm) => main },
          ( main: String) => ("", "")
       )   
    )(Password.apply)(Password.unapply)
  )

  /*
  * http POST function. 
  */

  def editUser = withAuth { username =>  implicit request =>

    val authUser = Auth.username(request).getOrElse(null)
    val aUser:User = User.findByName(authUser).getOrElse(null)
    val newUserForm = userForm.bindFromRequest()

    newUserForm.fold(
      hasErrors = { form =>
        val flash = play.api.mvc.Flash(Map("error" -> "Something went wrong, maybe passwords did not match"))
        BadRequest(views.html.user.page(aUser, form)(flash))},
      success = { update =>
        User.updateNamePass(aUser, update.password)
        Redirect(routes.Userpage.showUserPage(authUser)).flashing(
          "success" -> "Password change successful!")
      } 
    )
  }

  def deleteUser(name: String) = Action {
    User.delete(name)
    Redirect(routes.Auth.authenticate).withNewSession.flashing(
      "success" -> "User account deleted!")
  }
}
