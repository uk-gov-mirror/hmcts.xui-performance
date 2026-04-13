package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Login.xuiUrl
import utils.{Common, Environment, Headers}

object Caseworker_Navigation {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  /*====================================================================================
  *Apply a filter from the dropdowns (State = Submitted)
  *=====================================================================================*/

  val ApplyFilter =

    group("XUI_Caseworker_030_ApplyFilter") {
      exec(http("XUI_Caseworker_030_005_ApplyFilter")
        .post("/data/internal/searchCases?ctid=#{caseType}&use_case=WORKBASKET&view=WORKBASKET&state=AwaitingFinalOrder&page=1") //state is specific to NFD
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .formParam("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"size":25}"""))
        .check(substring("columns"))
        .check(jsonPath("$.total").saveAs("numberOfResults"))
        .check(jsonPath("$.results[*].case_id").findRandom.optional.saveAs("caseId")))
    }

    .exec(getCookieValue(CookieKey("__auth__").saveAs("authToken")))

    .pause(MinThinkTime, MaxThinkTime)

  /*====================================================================================
  *Sort By Last Modified Date
  *=====================================================================================*/

  val SortByLastModifiedDate =

    group("XUI_Caseworker_040_SortByLastModifiedDate") {
      exec(http("XUI_Caseworker_040_005_SortByLastModifiedDate")
        .post("/data/internal/searchCases?ctid=#{caseType}&use_case=WORKBASKET&view=WORKBASKET&state=AwaitingFinalOrder&page=1") //state is specific to NFD
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .formParam("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"sort":{"column":"[LAST_MODIFIED_DATE]","order":1,"type":"DateTime"},"size":25}"""))
        .check(substring("columns")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*====================================================================================
  *Navigate to Page 2
  *=====================================================================================*/

  val LoadPage2 =

  //Only load page 2 if there were more than 25 results returned
    doIf(session => session("numberOfResults").as[Int] > 25) {

      group("XUI_Caseworker_050_LoadPage2") {
        exec(http("XUI_Caseworker_050_005_LoadPage2")
          .post("/data/internal/searchCases?ctid=#{caseType}&use_case=WORKBASKET&view=WORKBASKET&state=AwaitingFinalOrder&page=2") //&state=Any
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .formParam("x-xsrf-token", "#{XSRFToken}")
          .body(StringBody("""{"sort":{"column":"[LAST_MODIFIED_DATE]","order":1,"type":"DateTime"},"size":25}"""))
          .check(substring("columns")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    }

  /*====================================================================================
  *Search by Case Number
  *=====================================================================================*/

  val SearchByCaseNumber =

    group("XUI_Caseworker_060_SearchByCaseNumber") {
      exec(http("XUI_Caseworker_060_005_SearchByCaseNumber")
        .post("/data/internal/searchCases?ctid=#{caseType}&use_case=WORKBASKET&view=WORKBASKET&state=AwaitingFinalOrder&page=1&case_reference=#{caseId}") //&state=Any
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .formParam("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"size":25}"""))
        .check(substring("columns"))
        .check(jsonPath("$.total").ofType[Int].is(1)))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*====================================================================================
  *View Case
  *=====================================================================================*/

  val ViewCase =

    group("XUI_Caseworker_070_ViewCase") {

      exec(http("XUI_Caseworker_070_005_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .header("experimental", "true")
        .check(jsonPath("$.case_id").is("#{caseId}"))
        .check(jsonPath("$.tabs[?(@.show_condition==null)].label").findAll.saveAs("tabNames"))
        //find all the document urls and capture only the document identifiers e.g. d513fccc-44e1-4326-880a-38a7627355ef
        .check(jsonPath("$.tabs[*].fields[*].value[*].value.documentLink.document_url").findAll.transform( x =>
        {
          val pattern = "/documents/([0-9a-z-]+)".r
          x.map(pattern.findAllIn(_).group(1))
        }).optional.saveAs("documentUrl"))
      )

      .exec(Common.caseActivityPost)
      .pause(2)
      .exec(Common.caseActivityGet)

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*====================================================================================
  *Navigate To Each Tab
  *=====================================================================================*/

  val NavigateTabs =

    foreach("#{tabNames}", "tabName") {

      group("XUI_Caseworker_080_NavigateTabs") {
        exec(ViewCase)

        .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F#{caseId}%23#{tabName}".replace(" ", "%2520")))

      }

      .pause(MinThinkTime, MaxThinkTime)

    }

  /*====================================================================================
  *View Documents (if applicable)
  *=====================================================================================*/

  val ViewDocument =

    foreach("#{documentUrl}", "documentLink") {

      group("XUI_Caseworker_090_ViewDocument") {

        exec(http("XUI_Caseworker_090_005_MediaViewer")
          .get("/media-viewer")
          .headers(Headers.navigationHeader)
          .header("sec-fetch-site", "none")
          .check(substring("HMCTS Manage cases")))

        //re-injecting the valid xui-webapp cookie, as for an unknown reason, XUI or Gatling changes the cookie value between
        //these two steps (media viewer and binary download), so it was trying to use an invalid cookie to retrieve
        //the doc, and throwing an http 401. This is the same as the 401 issue at login. Cause is unknown.
        .exec(addCookie(Cookie("xui-webapp", "#{xuiWebAppCookie}")
          .withMaxAge(28800)
          .withDomain(xuiUrl.replace("https://", ""))
          .withSecure(true)))

        .exec(http("XUI_Caseworker_090_010_ViewDocument")
          .get("/documentsv2/#{documentLink}/binary")
          .headers(Headers.commonHeader)
          .header("Accept", "*/*")
          .header("content-type", "")
          .check(responseTimeInMillis.saveAs("responseTime"))
          .check(bodyBytes.transform(_.length > 2000).is(true)) //200000
          )
      }

      .exec(Common.caseActivityPost)
      .pause(2)
      .exec(Common.caseActivityGet)

      .pause(MinThinkTime, MaxThinkTime)

    }

  /*====================================================================================
  *Return to the Case List
  *=====================================================================================*/

  val LoadCaseList =

    group("XUI_Caseworker_100_CaseList") {
        exec(http("XUI_100_005_Jurisdictions")
          .get("/aggregated/caseworkers/:uid/jurisdictions?access=read")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("id")))
          
      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

      .exec(Common.orgDetails)

      .exec(Common.userDetails)

      .exec(http("XUI_100_010_WorkBasketInputs")
        .get("/data/internal/case-types/#{caseType}/work-basket-inputs")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-workbasket-input-details.v2+json;charset=UTF-8")
        .check(regex("workbasketInputs|Not Found"))
        .check(status.in(200, 401, 404)))

      .exec(http("XUI_100_015_SearchCases")
        .post("/data/internal/searchCases?ctid=#{caseType}&use_case=WORKBASKET&view=WORKBASKET&page=1")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .formParam("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"size":25}"""))
        .check(substring("columns")))
    }

    .exec(Common.caseActivityPost)

    .exec(Common.caseActivityGet)

    .pause(MinThinkTime, MaxThinkTime)

}


