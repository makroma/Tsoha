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
    Renders moviepage or return 404
    */  

  def showUserPage(usern: String) = Action { implicit request =>

    val authUser = Auth.username(request).getOrElse(null)
    
    if(usern == authUser) {
      val user:User = User.findByName(authUser).getOrElse(null)

      val form = {
        if (flash.get("error").isDefined) userForm.bind(flash.data)
        else userForm.fill(new PassWord(user.userpassword))
      }

      Ok(views.html.user.page(user, form))
    } 
    else NotFound
  }

  /*
  Userform for editing existing user
  */

  val userForm:Form[PassWord]= Form(
    mapping(
      "password" -> tuple(
        "main" -> nonEmptyText,
        "confirm" -> nonEmptyText).verifying( 
          "Passwords don't match", password => password._1 == password._2 
        ).transform(
          { case (main, confirm) => main },
          ( main: String) => ("", "")
       )   
    )(PassWord.apply)(PassWord.unapply)
  )

  /*
  * http post function. 
  */

  def editUser = withAuth { username =>  implicit request =>
    print("editUser ")
    val authUser = Auth.username(request).getOrElse(null)
    val aUser:User = User.findByName(authUser).getOrElse(null)

    val newUserForm = userForm.bindFromRequest()
    newUserForm.fold(
      hasErrors = { form =>
        println("some error")
        BadRequest(views.html.user.page(aUser, form)).flashing(
          "error" -> "Something went wrong, maybe username is taken or passwords didnt match ")},
      success = { user =>
        User.updateNamePass(aUser, user.password) 
        println("update successfull.")
         Redirect(routes.Userpage.showUserPage(authUser)).flashing(
          "success" -> "Password change successfull!")
      } 
    )
  }
}
