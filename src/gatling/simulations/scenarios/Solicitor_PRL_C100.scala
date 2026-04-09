package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_C100 {
  
  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CreatePrivateLawCase =

    /*======================================================================================
    * Click the Create Case link
    ======================================================================================*/

    group("XUI_PRL_C100_030_CreateCase") {

      exec(_.setAll(
        "C100ApplicantFirstName1" -> ("App" + Common.randomString(5)),
        "C100ApplicantLastName1" -> ("Test" + Common.randomString(5)),
        "C100ApplicantFirstName2" -> ("App" + Common.randomString(5)),
        "C100ApplicantLastName2" -> ("Test" + Common.randomString(5)),
        "C100RespondentFirstName" -> ("Resp" + Common.randomString(5)),
        "C100RespondentLastName" -> ("Test" + Common.randomString(5)),
        "C100ChildFirstName" -> ("Child" + Common.randomString(5)),
        "C100ChildLastName" -> ("Test" + Common.randomString(5)),
        "C100RepresentativeFirstName" -> ("Rep" + Common.randomString(5)),
        "C100RepresentativeLastName" -> ("Test" + Common.randomString(5)),
        "C100SoleTraderName" -> ("Sole" + Common.randomString(5)),
        "C100SolicitorName" -> ("Soli" + Common.randomString(5)),
        "C100AppDobDay" -> Common.getDay(),
        "C100AppDobMonth" -> Common.getMonth(),
        "C100AppDobYear" -> Common.getDobYear(),
        "C100AppDobDay2" -> Common.getDay(),
        "C100AppDobMonth2" -> Common.getMonth(),
        "C100AppDobYear2" -> Common.getDobYear(),
        "C100ChildAppDobDay" -> Common.getDay(),
        "C100ChildAppDobMonth" -> Common.getMonth(),
        "C100ChildDobYear" -> Common.getDobYearChild(),
        "C100RespDobDay" -> Common.getDay(),
        "C100RespDobMonth" -> Common.getMonth(),
        "C100RespDobYear" -> Common.getDobYear()))

      .exec(http("XUI_PRL_C100_030_CreateCase")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("PRIVATELAW")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction = Family Private Law; Case Type = C100 & FL401 Applications; Event = Solicitor Application
    ======================================================================================*/

    .group("XUI_PRL_C100_040_SelectCaseType") {
      exec(http("XUI_FPL_040_005_StartApplication")
        .get("/data/internal/case-types/PRLAPPS/event-triggers/solicitorCreate?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitorCreate")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Type of Application (C100 or FL401) - C100
    ======================================================================================*/

    .group("XUI_PRL_C100_050_SelectApplicationType") {
      exec(http("XUI_PRL_C100_050_005_SelectApplicationType")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSelectApplicationType.json"))
        .check(substring("caseTypeOfApplication")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confidentiality Statement
    ======================================================================================*/

    .group("XUI_PRL_C100_060_ConfidentialityStatement") {
      exec(http("XUI_PRL_C100_060_005_ConfidentialityStatement")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLConfidentialityStatement.json"))
        .check(substring("c100ConfidentialityStatementDisclaimer")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select the Family Court
    ======================================================================================*/

    .group("XUI_PRL_C100_070_SelectFamilyCourt") {
      exec(http("XUI_PRL_C100_070_005_SelectFamilyCourt")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate5")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSelectFamilyCourt.json"))
        .check(substring("submitCountyCourtSelection")))

        .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Case Name
    ======================================================================================*/

    .group("XUI_PRL_C100_080_CaseName") {
      exec(http("XUI_PRL_C100_080_005_CaseName")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate6")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLCaseName.json"))
        .check(substring("applicantCaseName")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_090_CheckYourAnswers") {
      exec(http("XUI_PRL_C100_090_005_CheckYourAnswers")
        .post("/data/case-types/PRLAPPS/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLCheckYourAnswers.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_090_010_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val TypeOfApplication =

    /*======================================================================================
    * Click on 'Type of Application' link
    ======================================================================================*/

    group("XUI_PRL_C100_100_CreateTypeOfApplicationEvent") {
      exec(http("XUI_PRL_C100_100_005_CreateTypeOfApplicationViewCase")
        .get("/cases/case-details/#{caseId}/trigger/selectApplicationType/selectApplicationType1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_100_010_CreateTypeOfApplicationEventLink")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_100_015_CreateTypeOfApplicationEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/selectApplicationType?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("selectApplicationType")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Type of Application Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_110_TypeOfApplicationProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What order(s) are you applying for? - Child Arrangements, Spend Time with Order
    ======================================================================================*/

    .group("XUI_PRL_C100_120_SelectOrders") {
      exec(http("XUI_PRL_C100_120_005_SelectOrders")
        .post("/data/case-types/PRLAPPS/validate?pageId=selectApplicationType1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSelectOrders.json"))
        .check(substring("typeOfChildArrangementsOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Draft Consent Order Upload
    ======================================================================================*/

    .group("XUI_PRL_C100_130_ConsentOrderUpload") {
      exec(http("XUI_PRL_C100_130_005_ConsentOrderUpload")
        .post("/documentsv2")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "#{XSRFToken}")
        .bodyPart(RawFileBodyPart("files", "3MB.pdf")
          .fileName("3MB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "PRLAPPS")
        .formParam("jurisdictionId", "PRIVATELAW")
        .check(substring("originalDocumentName"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("documentHash"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you have a draft consent order? - Yes
    ======================================================================================*/

    .group("XUI_PRL_C100_140_ConsentOrder") {
      exec(http("XUI_PRL_C100_140_005_ConsentOrder")
        .post("/data/case-types/PRLAPPS/validate?pageId=selectApplicationType2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLConsentOrders.json"))
        .check(substring("consentOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    .group("XUI_PRL_C100_145_PermissionUpload") {
        exec(http("XUI_PRL_C100_145_005_PermissionUpload")
          .post("/documents")
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
          .header("content-type", "multipart/form-data")
          .header("x-xsrf-token", "#{XSRFToken}")
          .bodyPart(RawFileBodyPart("files", "7PageDoc.pdf")
            .fileName("7PageDoc.pdf")
            .transferEncoding("binary"))
          .asMultipartForm
          .formParam("classification", "PUBLIC")
          .formParam("caseTypeId", "PRLAPPS")
          .formParam("jurisdictionId", "PRIVATELAW")
          .check(substring("originalDocumentName"))
          .check(jsonPath("$._embedded.documents[0]._links.self.href").saveAs("DocumentURL_permission")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Have you applied to the court for permission to make this application? - Yes
    ======================================================================================*/

    .group("XUI_PRL_C100_150_PermissionForApplication") {
      exec(http("XUI_PRL_C100_150_005_PermissionForApplication")
        .post("/data/case-types/PRLAPPS/validate?pageId=selectApplicationType3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLPermissionRequired.json"))
        .check(substring("orderInPlacePermissionRequired")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide Brief Details of Application
    ======================================================================================*/

    .group("XUI_PRL_C100_160_ProvideBriefDetails") {
      exec(http("XUI_PRL_C100_160_005_ProvideBriefDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=selectApplicationType4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLProvideBriefDetails.json"))
        .check(substring("applicationDetails")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_170_CheckYourAnswers") {
      exec(http("XUI_PRL_C100_170_005_CheckYourAnswers")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLCheckYourAnswersTypeOfApplication.json"))
        .check(substring("applicationPermissionRequired"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_170_010_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='selectApplicationType')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val HearingUrgency =

    /*======================================================================================
    * Click on 'Hearing Urgency'
    ======================================================================================*/

    group("XUI_PRL_C100_180_HearingUrgency") {
      exec(http("XUI_PRL_C100_180_005_HearingUrgencyRedirect")
        .get("/cases/case-details/#{caseId}/trigger/hearingUrgency/hearingUrgency1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_180_010_HearingUrgencyViewCase")
        .get("/workallocation/case/tasks/#{caseId}/event/otherChildNotInTheCase/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.navigationHeader)
        .header("accept", "application/json")
        .check(substring("task_required_for_event")))

      .exec(http("XUI_PRL_C100_180_015_HearingUrgencyViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_180_020_HearingUrgencyEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/hearingUrgency?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("hearingUrgency")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Urgency Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_190_HearingUrgencyProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Urgency Questions
    ======================================================================================*/

    .group("XUI_PRL_C100_200_HearingUrgencyQuestions") {
      exec(http("XUI_PRL_C100_200_005_HearingUrgencyQuestions")
        .post("/data/case-types/PRLAPPS/validate?pageId=hearingUrgency1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLHearingUrgency.json"))
        .check(substring("areRespondentsAwareOfProceedings")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Urgency Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_210_HearingUrgencyCheckYourAnswers") {
      exec(http("XUI_PRL_C100_320_005_HearingUrgencyCheckYourAnswers")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLHearingUrgencyAnswers.json"))
        .check(substring("trigger/hearingUrgency")))

      .exec(http("XUI_PRL_C100_210_010_HearingUrgencyViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='hearingUrgency')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)

  val ApplicantDetails =

    /*======================================================================================
    * Click on 'Applicant Details'
    ======================================================================================*/

    group("XUI_PRL_C100_220_ApplicantDetails") {
      exec(http("XUI_PRL_C100_220_005_ApplicantDetailsRedirect")
        .get("/cases/case-details/#{caseId}/trigger/applicantsDetails/applicantsDetails1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_220_005_ApplicantDetailsViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_220_010_ApplicantDetailsEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/applicantsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("applicantsDetails")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Details Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_230_ApplicantDetailsProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Add New - 2 applicants to be added
    ======================================================================================*/

    .group("XUI_PRL_C100_240_ApplicantDetails") {
      exec(Common.caseShareOrgs)
      .exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_240_015_ApplicantDetailValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=applicantsDetails1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLApplicantDetails.json"))
        .check(substring("dxNumber")))

      .exec(Common.userDetails)
      .exec(Common.caseShareOrgs)
    }


    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Details Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_250_ApplicantDetailsCheckYourAnswers") {
      exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_250_005_ApplicantDetailsCheckYourAnswers")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLApplicantDetailsAnswers.json"))
        .check(substring("trigger/applicantsDetails")))

      .exec(http("XUI_PRL_C100_250_010_ApplicantDetailsViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='applicantsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ChildDetails =

    /*======================================================================================
    * Click on 'Child Details'
    ======================================================================================*/

    group("XUI_PRL_C100_260_ChildDetailsRedirect") {
      exec(http("XUI_PRL_C100_260_005_ChildDetailsRedirect")
        .get("/case-details/#{caseId}/trigger/childDetailsRevised/childDetailsRevised1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_260_010_ChildDetailsCaseView")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_260_015_ChildDetailsEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/childDetailsRevised?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[1].value[0].value.whoDoesTheChildLiveWith.list_items[*].code").findAll.saveAs("childLiveWithCode"))
        .check(jsonPath("$.case_fields[1].value[0].value.whoDoesTheChildLiveWith.list_items[*].label").findAll.saveAs("childLiveWithLabel"))
        .check(jsonPath("$.case_fields[1].value[0].id").saveAs("childLiveWithId"))        
        .check(jsonPath("$.id").is("childDetailsRevised")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_270_ChildDetailsProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Add New Child
    ======================================================================================*/

    .group("XUI_PRL_C100_280_ChildDetailsAddNew") {
      exec(http("XUI_PRL_C100_280_005_ChildDetailsAddNew")
        .post("/data/case-types/PRLAPPS/validate?pageId=childDetailsRevised1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildDetails.json"))
        .check(substring("newChildDetails")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Additional Details
    ======================================================================================*/

    .group("XUI_PRL_C100_290_ChildDetailsAdditionalDetails") {
      exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_290_005_ChildDetailsAdditionalDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=childDetailsRevised2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildAdditionalDetails.json"))
        .check(substring("childrenKnownToLocalAuthority")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Answer Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_300_ChildDetailsAdditionalDetails") {
      exec(http("XUI_PRL_C100_300_005_ChildDetailsAdditionalDetails")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildDetailsEvent.json"))
        .check(substring("trigger/childDetailsRevised")))

      .exec(http("XUI_PRL_C100_300_010_ChildDetailsAdditionalDetailsViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='childDetailsRevised')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val RespondentDetails =

    /*======================================================================================
    * Click on 'Respondent Details'
    ======================================================================================*/

    group("XUI_PRL_C100_310_RespondentDetailsRedirect") {
      exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_310_005_RespondentDetailsRedirect")
        .get("/cases/case-details/#{caseId}/trigger/respondentsDetails/respondentsDetails1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_310_010_RespondentDetailsCaseView")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_310_015_RespondentDetailsCaseEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/respondentsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(substring("Details of the respondents in the case")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_320_RespondentDetailsProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Add Respondent Details
    ======================================================================================*/

    .group("XUI_PRL_C100_330_RespondentDetailsAddNew") {
      exec(Common.caseShareOrgs)
      .exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_330_005_RespondentDetailsAddNew")
        .post("/data/case-types/PRLAPPS/validate?pageId=respondentsDetails1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLRespondentDetails.json"))
        .check(substring("isAtAddressLessThan5YearsWithDontKnow")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_340_RespondentDetailsSubmit") {
      exec(http("XUI_PRL_C100_340_005_RespondentDetailsSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildAdditionalDetailsSubmit.json"))
        .check(substring("trigger/respondentsDetails")))

      .exec(http("XUI_PRL_C100_340_01_RespondentDetailsSubmitViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val MIAM =

    /*======================================================================================
    * Click on 'Miam'
    ======================================================================================*/

    group("XUI_PRL_C100_350_MIAMRedirect") {
      exec(http("XUI_PRL_C100_350_005_MIAMRedirect")
        .get("/cases/case-details/trigger/miam/miam1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      //see xui-webapp cookie capture in the Homepage scenario for details of why this is being used.
      //after a period of time during a performance test, the cookie would change and subsequent calls would fail
      //with a 401 unauthorized, so this code is forcing the original cookie back in to the Gatling session
      .exec(addCookie(Cookie("xui-webapp", "#{xuiWebAppCookie}").withMaxAge(28800).withSecure(true)))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_350_010_MIAMCaseView")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_350_015_MIAMCaseEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/respondentsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(substring("submissionRequiredFieldsInfo1")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_360_MIAMProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM Details
    ======================================================================================*/

    .group("XUI_PRL_C100_370_MIAMValidate") {
      exec(http("XUI_PRL_C100_370_005_MIAMValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=miamPolicyUpgrade1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLMIAMDetails.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_380_MIAMSubmit") {
      exec(http("XUI_PRL_C100_380_005_MIAMSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLMIAMDetailsSubmit.json"))
        .check(substring("trigger/miam")))

      .exec(http("XUI_PRL_C100_380_010_MIAMViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='miamPolicyUpgrade')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val AllegationsOfHarm =

    /*======================================================================================
    * Click on 'Allegations Of Harm'
    ======================================================================================*/

    group("XUI_PRL_C100_390_AllegationsOfHarmRedirect") {
      exec(http("XUI_PRL_C100_390_005_AllegationsOfHarmRedirect")
        .get("/cases/case-details/#{caseId}/trigger/allegationsOfHarmRevised/allegationsOfHarmRevised1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_390_010_AllegationsOfHarmRedirectCaseView")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}"))

      .exec(http("XUI_PRL_C100_390_015_AllegationsOfHarmEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/allegationsOfHarmRevised?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_400_AllegationsOfHarmProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are there Allegations of Harm?
    ======================================================================================*/

    .group("XUI_PRL_C100_410_AllegationsOfHarm") {
      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_410_005_AllegationsOfHarm")
        .post("/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarm1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAreThereAllegationsOfHarm.json")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm details
    ======================================================================================*/

    .group("XUI_PRL_C100_420_AllegationsOfHarmDetails") {
      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_420_005_AllegationsOfHarmDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarm2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAllegationsOfHarmDetails.json")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Behaviour
    ======================================================================================*/

    .group("XUI_PRL_C100_430_AllegationsOfHarmBehaviour") {
      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_430_005_AllegationsOfHarmBehaviour")
        .post("/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarm3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAllegationsOfHarmBehaviour.json")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Other Concerns
    ======================================================================================*/

    .group("XUI_PRL_C100_440_AllegationsOfHarmOther") {
      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_440_005_AllegationsOfHarmOther")
        .post("/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarm4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAllegationsOfHarmOther.json")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_450_AllegationsOfHarmSubmit") {
      exec(http("XUI_PRL_C100_450_005_AllegationsOfHarmSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAreThereAllegationsOfHarmSubmit.json")))

      .exec(http("XUI_PRL_C100_450_010_AllegationsOfHarmViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='allegationsOfHarmRevised')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val OtherChildrenNotInCase =

    /*======================================================================================
    * Click on 'Other children not in the case'
    ======================================================================================*/

    group("XUI_PRL_C100_460_OtherChildrenNotInCase") {
      exec(http("XUI_PRL_C100_460_005_OtherChildrenNotInCase")
        .get("/cases/case-details/#{caseId}/trigger/otherChildNotInTheCase/otherChildNotInTheCase1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_460_010_OtherChildrenNotInCase")
        .get("/workallocation/case/tasks/#{caseId}/event/otherChildNotInTheCase/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.navigationHeader)
        .header("accept", "application/json")
        .check(substring("task_required_for_event")))

      .exec(http("XUI_PRL_C100_460_015_OtherChildrenNotInCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_460_020_OtherChildrenNotInCase")
        .get("/data/internal/cases/#{caseId}/event-triggers/otherChildNotInTheCase?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(substring("Other children not in the case")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * No other children in application
    ======================================================================================*/

    .group("XUI_PRL_C100_470_OtherChildrenNotInCaseValidate") {
      exec(http("XUI_PRL_C100_470_005_OtherChildrenNotInCaseValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=otherChildNotInTheCase1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLOtherChildrenValidate.json"))
        .check(substring("childrenNotPartInTheCaseYesNo")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Other Children Not In Case event
    ======================================================================================*/

    .group("XUI_PRL_C100_480_OtherChildrenNotInCaseSubmit") {
      exec(http("XUI_PRL_C100_480_005_OtherChildrenNotInCaseSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .body(ElFileBody("bodies/prl/c100/PRLOtherChildrenSubmit.json"))
        .check(substring("trigger/otherChildNotInTheCase")))

      .exec(http("XUI_PRL_C100_480_005_OtherChildrenNotInCaseSubmit")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.events[?(@.event_id=='otherChildNotInTheCase')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.manageLabellingRoleAssignment)
      .exec(Common.waJurisdictions)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val OtherPeopleInCase = 

    group("XUI_PRL_C100_490_OtherPeopleInTheCase") {
      exec(http("XUI_PRL_C100_490_005_OtherPeopleInTheCase")
        .get("/cases/case-details/#{caseId}/trigger/otherPeopleInTheCaseRevised/otherPeopleInTheCaseRevised1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.userDetails)
          
      .exec(http("XUI_PRL_C100_490_010_OtherPeopleInTheCase")
        .get("/workallocation/case/tasks/#{caseId}/event/otherPeopleInTheCaseRevised/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.navigationHeader)
        .header("accept", "application/json")
        .check(substring("task_required_for_event")))

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_490_015_OtherPeopleInTheCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_PRL_C100_490_020_OtherPeopleInTheCase")
        .get("/data/internal/cases/#{caseId}/event-triggers/otherPeopleInTheCaseRevised?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(substring("Other people in the case")))

      .exec(Common.profile)
      .exec(Common.caseShareOrgs)
    }
    
    .pause(MinThinkTime, MaxThinkTime)

    .group("XUI_PRL_C100_500_OtherPeopleInTheCaseValidate") {
      exec(http("XUI_PRL_C100_500_005_OtherPeopleInTheCaseValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=otherPeopleInTheCaseRevised1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLOtherPeopleValidate.json"))
        .check(substring("isPlaceOfBirthKnown"))
        .check(substring("otherPersonRelationshipToChildren")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    .group("XUI_PRL_C100_510_OtherPeopleInTheCaseSubmit") {
      exec(http("XUI_PRL_C100_510_005_OtherPeopleInTheCaseSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .body(ElFileBody("bodies/prl/c100/PRLOtherPeopleSubmit.json"))
        .check(substring("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_510_010_OtherPeopleInTheCaseSubmit")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(substring("Application for a court order to make arrangements for a child")))
            
      .exec(Common.waJurisdictions)
      .exec(Common.manageLabellingRoleAssignment)
    }

  val ChildrenAndApplicants = 

    /*======================================================================================
    * Click on 'Children and applicants'
    ======================================================================================*/

    group("XUI_PRL_C100_520_ChildrenAndApplicants") {
      exec(http("XUI_PRL_C100_520_005_ChildrenAndApplicants")
        .get("/cases/case-details/#{caseId}/trigger/childrenAndApplicants/childrenAndApplicants1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_520_010_ChildrenAndApplicants")
        .get("/workallocation/case/tasks/#{caseId}/event/childrenAndApplicants/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.navigationHeader)
        .header("accept", "application/json")
        .check(substring("task_required_for_event")))

      .exec(http("XUI_PRL_C100_520_015_ChildrenAndApplicants")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_520_020_ChildrenAndApplicants")
        .get("/data/internal/cases/#{caseId}/event-triggers/childrenAndApplicants?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[?(@.id=='buffChildAndApplicantRelations')].value[0].id").saveAs("applicant_one"))
        .check(jsonPath("$.case_fields[?(@.id=='buffChildAndApplicantRelations')].value[1].id").saveAs("applicant_two"))
        .check(jsonPath("$.case_fields[?(@.id=='buffChildAndApplicantRelations')].formatted_value[0].value.applicantId").saveAs("applicant_oneId"))
        .check(jsonPath("$.case_fields[?(@.id=='buffChildAndApplicantRelations')].formatted_value[1].value.applicantId").saveAs("applicant_twoId"))
        .check(jsonPath("$.case_fields[?(@.id=='buffChildAndApplicantRelations')].formatted_value[1].value.applicantId").saveAs("childId"))
        .check(substring("Create a Relation between Children and Applicants")))

      .exec(Common.userDetails)
      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Review and confirm details'
    ======================================================================================*/

    .group("XUI_PRL_C100_530_ChildrenAndApplicantsValidate") {
      exec(http("XUI_PRL_C100_530_ChildrenAndApplicantsValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=childrenAndApplicants1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildrenAndApplicantValidate.json"))
        .check(substring("buffChildAndApplicantRelations")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit the case event
    ======================================================================================*/

    .group("XUI_PRL_C100_540_ChildrenAndApplicantsSubmit") {
      exec(http("XUI_PRL_C100_540_005_ChildrenAndApplicantsSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .body(ElFileBody("bodies/prl/c100/PRLChildrenAndApplicantSubmit.json"))
        .check(substring("trigger/childrenAndApplicants")))

      .exec(http("XUI_PRL_C100_540_010_ChildrenAndApplicantsSubmit")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.events[?(@.event_id=='childrenAndApplicants')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.manageLabellingRoleAssignment)
      .exec(Common.waJurisdictions)
      .exec(Common.userDetails)
    }

  .pause(MinThinkTime, MaxThinkTime)

  val ChildrenAndRespondents = 

    /*======================================================================================
    * Click on 'Children and respondents'
    ======================================================================================*/
    
    group("XUI_PRL_C100_550_ChildrenAndRespondents") {
      exec(http("XUI_PRL_C100_550_005_ChildrenAndRespondents")
        .get("/cases/case-details/#{caseId}/trigger/childrenAndRespondents/childrenAndRespondents1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_550_010_ChildrenAndRespondents")
        .get("/workallocation/case/tasks/#{caseId}/event/childrenAndRespondents/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.navigationHeader)
        .header("accept", "application/json")
        .check(substring("task_required_for_event")))

      .exec(http("XUI_PRL_C100_550_015_ChildrenAndRespondents")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(substring("Application for a court order to make arrangements for a child")))

      .exec(http("XUI_PRL_C100_550_020_ChildrenAndRespondents")
        .get("/data/internal/cases/#{caseId}/event-triggers/childrenAndRespondents?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[2].value[0].value.respondentFullName").saveAs("respondentName"))
        .check(jsonPath("$.case_fields[2].value[0].value.childFullName").saveAs("childName"))
        .check(jsonPath("$.case_fields[2].value[0].id").saveAs("respondentId"))
        .check(jsonPath("$.case_fields[2].value[0].value.respondentId").saveAs("respondentNameId"))
        .check(jsonPath("$.case_fields[2].value[0].value.childId").saveAs("childId"))
        .check(substring("Create a Relation between Children and Respondents")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter details of a Respondent and click Continue
    ======================================================================================*/

    .group("XUI_PRL_C100_560_ChildrenAndRespondentsValidate") {
      exec(http("XUI_PRL_C100_560_005_ChildrenAndRespondentsValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=childrenAndRespondents1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildrenAndRespondentsValidate.json"))
        .check(substring("buffChildAndRespondentRelations")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confirm details and Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_570_ChildrenAndRespondentsSubmit") {
      exec(http("XUI_PRL_C100_570_005_ChildrenAndRespondentsSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .body(ElFileBody("bodies/prl/c100/PRLChildrenAndRespondentsSubmit.json"))
        .check(substring("trigger/childrenAndRespondents")))


      .exec(http("XUI_PRL_C100_570_010_ChildrenAndRespondentsSubmit")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.events[?(@.event_id=='childrenAndRespondents')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.manageLabellingRoleAssignment)
      .exec(Common.waJurisdictions)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ChildrenAndOtherPeople = 

    group("XUI_PRL_580_ChildrenAndOtherPeople") {
      exec(http("XUI_PRL_580_005_ChildrenAndOtherPeople")
        .get("/cases/case-details/#{caseId}/trigger/childrenAndOtherPeople/childrenAndOtherPeople1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))
          
      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.configUI)
      .exec(Common.TsAndCs)
      .exec(Common.isAuthenticated)
      .exec(Common.userDetails)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_580_010_ChildrenAndOtherPeople")
        .get("/workallocation/case/tasks/#{caseId}/event/childrenAndOtherPeople/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.navigationHeader)
        .header("accept", "application/json")
        .check(substring("task_required_for_event")))

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_518_015_ChildrenAndOtherPeople")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json"))

      .exec(http("XUI_PRL_580_020_ChildrenAndOtherPeople")
        .get("/data/internal/cases/#{caseId}/event-triggers/childrenAndOtherPeople?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[2].value[0].value.otherPeopleFullName").saveAs("otherPeopleFullName0"))
        .check(jsonPath("$.case_fields[2].value[0].value.otherPeopleId").saveAs("otherPeopleSubId0"))
        .check(jsonPath("$.case_fields[2].value[0].value.childFullName").saveAs("childFullName"))
        .check(jsonPath("$.case_fields[2].value[0].value.childId").saveAs("childId"))
        .check(jsonPath("$.case_fields[2].value[0].id").saveAs("otherPeopleId0"))
        .check(jsonPath("$.case_fields[2].value[1].value.otherPeopleFullName").saveAs("otherPeopleFullName1"))
        .check(jsonPath("$.case_fields[2].value[1].value.otherPeopleId").saveAs("otherPeopleSubId1"))
        .check(jsonPath("$.case_fields[2].value[1].id").saveAs("otherPeopleId1"))
        .check(substring("Create a Relation between Children and Other People")))
          
      .exec(Common.profile)
    }
        
    .pause(MinThinkTime, MaxThinkTime)

    .group("XUI_PRL_590_ChildrenAndOtherPeopleValidate") {
      exec(http("XUI_PRL_590_005_ChildrenAndOtherPeopleValidate")
        .post("/data/case-types/PRLAPPS/validate?pageId=childrenAndOtherPeople1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildrenAndOtherPeopleValidate.json"))
        .check(substring("buffChildAndOtherPeopleRelations")))
    }
      
    .pause(MinThinkTime, MaxThinkTime)

    .group("XUI_PRL_600_ChildrenAndOtherPeopleSubmit") {
      exec(http("XUI_PRL_600_005_ChildrenAndOtherPeopleSubmit")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .body(ElFileBody("bodies/prl/c100/PRLChildrenAndOtherPeopleSubmit.json"))
        .check(substring("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_600_010_ChildrenAndOtherPeopleSubmit")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(substring("Application for a court order to make arrangements for a child")))
            
      .exec(Common.waJurisdictions)
      .exec(Common.manageLabellingRoleAssignment)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ViewPdfApplication =

    /*======================================================================================
    * Click on 'View PDF Application'
    ======================================================================================*/

    group("XUI_PRL_C100_610_ViewPdfApplicationRedirect") {
      exec(http("XUI_PRL_C100_610_005_ViewPdfApplicationRedirect")
        .get("/cases/case-details/#{caseId}/trigger/viewPdfDocument/viewPdfDocument1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_610_010_ViewPdfApplicationRedirectCaseView")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}"))

      .exec(http("XUI_PRL_C100_610_015_ViewPdfApplicationRedirectEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/viewPdfDocument?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.case_fields[?(@.id=='submitAndPayDownloadApplicationLink')].value.document_url").saveAs("DocumentUrl"))
        .check(jsonPath("$.case_fields[?(@.id=='submitAndPayDownloadApplicationLink')].value.document_filename").saveAs("DocumentFileName"))
        .check(jsonPath("$.case_fields[?(@.id=='submitAndPayDownloadApplicationLink')].value.document_hash").saveAs("DocumentHash"))
        .check(jsonPath("$.event_token").saveAs("event_token")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * View PDF Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_620_ViewPdfProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * View PDF Continue
    ======================================================================================*/

    .group("XUI_PRL_C100_630_ViewPdfContinue") {
      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_630_005_ViewPdfContinue")
        .post("/data/case-types/PRLAPPS/validate?pageId=viewPdfDocument1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLViewPdfContinue.json"))
        .check(substring("isEngDocGen")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * View PDF Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_640_ViewPdfSubmit") {
      exec(http("XUI_PRL_C100_640_005_ViewPdfSubmitViewCase")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLViewPdfContinueSubmit.json")))

      .exec(http("XUI_PRL_C100_640_010_ViewPdfSubmit")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val SubmitAndPay =

    /*======================================================================================
    * Click on 'SubmitAndPay'
    ======================================================================================*/

    group("XUI_PRL_C100_650_SubmitAndPayRedirect") {
      exec(http("XUI_PRL_C100_650_005_SubmitAndPayRedirect")
        .get("/cases/case-details/#{caseId}/trigger/submitAndPay/submitAndPay1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)
      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_650_010_SubmitAndPayRedirectCaseView")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_650_015_SubmitAndPayRedirectEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/submitAndPay?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("submitAndPay")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_660_SubmitAndPayProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Confidentiality Statement
    ======================================================================================*/

    .group("XUI_PRL_C100_670_SubmitAndPayConfidentialityStatement") {
      exec(http("XUI_PRL_C100_670_005_SubmitAndPayConfidentialityStatement")
        .post("/data/case-types/PRLAPPS/validate?pageId=submitAndPay1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayConfidentialityStatement.json"))
        .check(substring("applicantSolicitorEmailAddress")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Declaration
    ======================================================================================*/

    .group("XUI_PRL_C100_680_SubmitAndPayDeclaration") {
      exec(http("XUI_PRL_C100_680_005_SubmitAndPayDeclaration")
        .post("/data/case-types/PRLAPPS/validate?pageId=submitAndPay2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayDeclaration.json"))
        .check(substring("feeAmount")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Continue
    ======================================================================================*/

    .group("XUI_PRL_C100_690_SubmitAndPayContinue") {
      exec(http("XUI_PRL_C100_690_005_SubmitAndPayContinue")
        .post("/data/case-types/PRLAPPS/validate?pageId=submitAndPay3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayContinue.json"))
        .check(substring("paymentServiceRequestReferenceNumber")))

      .exec(Common.userDetails)
    }
    
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Now
    ======================================================================================*/

    .group("XUI_PRL_C100_700_SubmitAndPayNow") {
      exec(http("XUI_PRL_C100_700_005_SubmitAndPayNow")
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayNow.json"))
        .check(substring("created_on")))

      .exec(http("XUI_PRL_C100_700_010_SubmitAndPayNowViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='submitAndPay')]"))
        .check(jsonPath("$.state.id").is("SUBMITTED_NOT_PAID")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    val HearingsTab = 

    /*======================================================================================
    * Click on the Hearings tab to view any Hearings
    ======================================================================================*/

    group("XUI_PRL_C100_710_HearingsTab") {
      exec(http("XUI_PRL_C100_710_GetHearings")
        .get("/api/hearings/getHearings?caseId=#{caseId}")
        .headers(Headers.commonHeader)
        .header("Accept", "application/json, text/plain, */*")
        .check(status.in(200, 403)))

      .exec(http("XUI_PRL_C100_710_GetHearingsJurisdiction")
        .post("/api/hearings/loadServiceHearingValues?jurisdictionId=PRIVATELAW")
        .headers(Headers.commonHeader)
        .header("Content-Type", "application/json; charset=utf-8")
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"caseReference":"#{caseId}"}"""))
        .check(substring("hearing-facilities")))

      .exec(http("XUI_PRL_C100_710_GetRoleAssignments")
        .get("/api/user/details?refreshRoleAssignments=undefined")
        .headers(Headers.commonHeader)
        .header("Accept", "application/json, text/plain, */*"))

      .exec(http("XUI_PRL_C100_710_GetHearingTypes")
        .get("/api/prd/lov/getLovRefData?categoryId=HearingType&serviceId=ABA5&isChildRequired=N")
        .headers(Headers.commonHeader)
        .header("Accept", "application/json, text/plain, */*")
        .check(substring("HearingType")))
    }

    .pause(MinThinkTime, MaxThinkTime)

}