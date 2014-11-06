package vfd.frontend.util

import scala.language.implicitConversions
import scala.util.Failure
import scala.util.Success

import org.scalajs.dom
import org.scalajs.dom.Element

import rx.Rx
import rx.Rx
import rx.core.Obs
import scalatags.JsDom.all.Attr
import scalatags.JsDom.all.AttrValue
import scalatags.JsDom.all.Frag
import scalatags.JsDom.all.HtmlTag
import scalatags.JsDom.all.Style
import scalatags.JsDom.all.backgroundColor
import scalatags.JsDom.all.bindNode
import scalatags.JsDom.all.span
import scalatags.JsDom.all.stringFrag
import scalatags.JsDom.all.stringStyle
import scalatags.JsDom.all.StyleValue

/**
 * A minimal binding between Scala.Rx and Scalatags and Scala-Js-Dom
 * taken from https://github.com/lihaoyi/workbench-example-app/blob/todomvc/src/main/scala/example/Framework.scala, by Li Haoyi
 */
object Framework {

  /**
   * Wraps reactive strings in spans, so they can be referenced/replaced
   * when the Rx changes.
   */
  implicit def RxStr[T](r: Rx[T])(implicit f: T => Frag): Frag = {
    rxMod(Rx(span(r())))
  }

  /**
   * Sticks some Rx into a Scalatags fragment, which means hooking up an Obs
   * to propagate changes into the DOM via the element's ID. Monkey-patches
   * the Obs onto the element itself so we have a reference to kill it when
   * the element leaves the DOM (e.g. it gets deleted).
   */
  implicit def rxMod[T <: dom.HTMLElement](r: Rx[HtmlTag]): Frag = {
    def rSafe = r.toTry match {
      case Success(v) => v.render
      case Failure(e) => span(e.toString, backgroundColor := "red").render
    }
    var last = rSafe
    Obs(r, skipInitial = true) {
      val newLast = rSafe
      last.parentElement.replaceChild(newLast, last)
      last = newLast
    }
    bindNode(last)
  }

  implicit def RxAttrValue[T: AttrValue] = new AttrValue[Rx[T]] {
    def apply(t: Element, a: Attr, r: Rx[T]): Unit = {
      Obs(r) { implicitly[AttrValue[T]].apply(t, a, r()) }
    }
  }

  implicit def RxStyleValue[T: StyleValue] = new StyleValue[Rx[T]] {
    def apply(t: Element, s: Style, r: Rx[T]): Unit = {
      Obs(r) { implicitly[StyleValue[T]].apply(t, s, r()) }
    }
  }
}
