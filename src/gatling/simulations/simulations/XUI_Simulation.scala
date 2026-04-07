package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.http.Predef._
import scenarios._
import utils._
import xui._
import elasticSearchFeeder._


import scala.concurrent.duration._
import scala.io.Source
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

class XUI_Simulation extends Simulation {

	val UserFeederPRL = csv("UserDataPRL.csv").circular
	val UserFeederPRLBarrister = csv("UserDataPRLBarristerList.csv").circular
	val UserFeederPRLCourtAdmin = csv("UserDataPRLCourtAdmin.csv").circular
	val UserFeederBails = csv("UserDataBails.csv").circular
	val UserFeederBailsHO = csv("UserDataBailsHO.csv").circular
	val UserFeederBailsAdmin = csv("UserDataBailsAdmin.csv").circular
	val UserFeederBailsJudge = csv("UserDataBailsJudge.csv").circular
	val UserFeederProbate = csv("UserDataProbate.csv").circular
	val UserFeederIAC = csv("UserDataIAC.csv").circular
	val UserFeederNFD = csv("UserDataNFD.csv").circular
	val UserFeederFR = csv("UserDataFR.csv").circular
	val UserFeederFPL = csv("UserDataFPL.csv").circular
	val CaseworkerUserFeeder = csv("UserDataCaseworkers.csv").circular
	val UserFeederCTSC = csv("UserDataCTSC.csv").circular
  val UserFeederLA = csv("UserDataPRLLocalAuthorityList.csv").circular
  val AddLAList = csv("AddLAList.csv").circular
  val LAData = csv("LAData.csv").circular
  val CWData = csv("CWData.csv").circular

	//Read in text labels required for each NFD case type - sole and joint case labels are different, so are fed directly into the JSON payload bodies
	val nfdSoleLabelsInitialised = Source.fromResource("bodies/nfd/labels/soleLabelsInitialised.txt").mkString
	val nfdSoleLabelsPopulated = Source.fromResource("bodies/nfd/labels/soleLabelsPopulated.txt").mkString
	val nfdJointLabelsInitialised = Source.fromResource("bodies/nfd/labels/jointLabelsInitialised.txt").mkString
	val nfdJointLabelsPopulated = Source.fromResource("bodies/nfd/labels/jointLabelsPopulated.txt").mkString

	val patternDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")

	/* TEST TYPE DEFINITION */
	/* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
	/* perftest (default) = performance test against the perftest environment */
	val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

	//set the environment based on the test type
	val environment = testType match {
		case "perftest" => "perftest"
		case "pipeline" => "perftest"
		case _ => "**INVALID**"
	}

	/* ******************************** */
	/* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
	val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
	val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
	/* ******************************** */

	/* PERFORMANCE TEST CONFIGURATION */
	val prlC100TargetPerHour: Double = 64
	val prlFL401TargetPerHour: Double = 36
	val prlTargetPerHour: Double =  90
	val bailsTargetPerHour: Double = 10
	val probateTargetPerHour: Double = 250
	val iacTargetPerHour: Double = 20
	val nfdSoleTargetPerHour: Double = 120
	val nfdJointTargetPerHour: Double = 120
	val fplTargetPerHour: Double = 30
	val frTargetPerHour: Double = 100
	val caseworkerTargetPerHour: Double = 1000
	val prlC100BarristerTargetPerHour: Double = 17
	val prlFL401BarristerTargetPerHour: Double = 11
  val prlC100LATargetPerHour: Double = 26

	val rampUpDurationMins = 5
	val rampDownDurationMins = 5
	val testDurationMins = 60

	val numberOfPipelineUsers = 5
	val pipelinePausesMillis: Long = 3000 //3 seconds

	//Determine the pause pattern to use:
	//Performance test = use the pauses defined in the scripts
	//Pipeline = override pauses in the script with a fixed value (pipelinePauseMillis)
	//Debug mode = disable all pauses
	val pauseOption: PauseType = debugMode match {
		case "off" if testType == "perftest" => constantPauses
		case "off" if testType == "pipeline" => customPauses(pipelinePausesMillis)
		case _ => disabledPauses
	}

	val httpProtocol = http
		.baseUrl(Environment.baseURL.replace("#{env}", s"${env}"))
		.inferHtmlResources()
		.silentResources
		.header("experimental", "true") //used to send through client id, s2s and bearer tokens. Might be temporary

	before {
		println(s"Test Type: ${testType}")
		println(s"Test Environment: ${env}")
		println(s"Debug Mode: ${debugMode}")
	}

	//Elastic search is being used to identify appropriate cases for Barrister scenarios
	val iterationsC100 = if (debugMode == "off") CalculateRecordsRequired.calculate(prlC100BarristerTargetPerHour*3, rampUpDurationMins, testDurationMins, rampDownDurationMins) else 1
	val iterationsFL401 = if (debugMode == "off") CalculateRecordsRequired.calculate(prlFL401BarristerTargetPerHour*3, rampUpDurationMins, testDurationMins, rampDownDurationMins) else 1
	val currentPath: String = System.getProperty("user.dir")

	/*val caseIdFeederC100 = ElasticSearchCaseFeeder.feeder(
		esIndices.PRL,
		s"${currentPath}/src/gatling/resources/bodies/prl/c100/C100_ElasticSearch_Query.json",
		FeederType.RANDOM,
		iterationsC100)

	val caseIdFeederFL401 = ElasticSearchCaseFeeder.feeder(
		esIndices.PRL,
		s"${currentPath}/src/gatling/resources/bodies/prl/fl401/FL401_ElasticSearch_Query.json",
		FeederType.RANDOM,
		iterationsFL401)

	def getCaseC100(caseIdFeederC100: Iterator[Map[String, Any]]) =
		feed(caseIdFeederC100)

  def getCaseFL401(caseIdFeederFL401: Iterator[Map[String, Any]]) =
		feed(caseIdFeederFL401)*/


  /*===============================================================================================
* XUI Solicitor Private Law C100 Scenario
===============================================================================================*/
  val PRLC100SolicitorScenario = scenario("***** Private Law Solicitor C100 Create Case *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        //Solicitor creates application
        .exec(Solicitor_PRL_C100.CreatePrivateLawCase)
        .exec(Solicitor_PRL_C100.TypeOfApplication)
        .exec(Solicitor_PRL_C100.HearingUrgency)
        .exec(Solicitor_PRL_C100.ApplicantDetails)
        .exec(Solicitor_PRL_C100.ChildDetails)
        .exec(Solicitor_PRL_C100.RespondentDetails)
        .exec(Solicitor_PRL_C100.AllegationsOfHarm)
        .exec(Solicitor_PRL_C100.OtherChildrenNotInCase)
        .exec(Solicitor_PRL_C100.OtherPeopleInCase)
        .exec(Solicitor_PRL_C100.ChildrenAndApplicants)
        .exec(Solicitor_PRL_C100.ChildrenAndRespondents)
        .exec(Solicitor_PRL_C100.ChildrenAndOtherPeople)
        .exec(Solicitor_PRL_C100.MIAM)
        .exec(Solicitor_PRL_C100.ViewPdfApplication)
        .exec(Solicitor_PRL_C100.SubmitAndPay)
        .exec(XuiHelper.Logout)
        .feed(UserFeederPRLCourtAdmin)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
        //Case worker progress case
        .exec(CourtAdmin_PRL_C100.CourtAdminCheckApplication)
        .exec(CourtAdmin_PRL_C100.CourtAdminSendToGateKeeper)
        .exec(CourtAdmin_PRL_C100.CourtAdminManageOrders)
        .exec(CourtAdmin_PRL_C100.CourtAdminServiceApplication)
        .exec(CourtAdmin_PRL_C100.CourtAdminListHearing)
        .exec(CourtAdmin_PRL_C100.CourtAdminHearingsTab)
        .exec(XuiHelper.Logout)
        /*.feed(UserFeederPRLCourtAdmin)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
        .exec(CourtAdmin_PRL_C100.PathfinderCase)
        .exec(CourtAdmin_PRL_C100.AddLocalAuthority)
        .exec(XuiHelper.Logout)*/
    }

  /*===============================================================================================
* XUI Add LA to C100 Scenario
===============================================================================================*/
  val PRLC100AddLAScenario = scenario("***** Private Law Add LA*******")
    .exitBlockOnFail {
      feed(UserFeederLA)
      .feed(UserFeederPRLCourtAdmin)
      .feed(AddLAList)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
        .exec(CourtAdmin_PRL_C100.PathfinderCase)
        .exec(CourtAdmin_PRL_C100.AddLocalAuthority)
        .exec(CourtAdmin_PRL_C100.writeToFile)
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
* XUI Add LA to C100 Scenario
===============================================================================================*/
  val PRLC100LAScenario = scenario("***** Private Law LA Tasks *****")
    .exitBlockOnFail {
        feed(LAData)
        .feed(UserFeederPRLCourtAdmin)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{LAUser}", "#{LAPassword}"))
        .exec(CourtAdmin_PRL_C100.LAOpenCase)
        .exec(CourtAdmin_PRL_C100.clickParties)
        .exec(CourtAdmin_PRL_C100.ManageDocsUpload)
        .exec(CourtAdmin_PRL_C100.caseFileView)
        .exec(XuiHelper.Logout)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
        .exec(CourtAdmin_PRL_C100.RemoveLocalAuthority)
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
* XUI Add LA to C100 Scenario
===============================================================================================*/
  val PRLC100CaseWorkerScenario = scenario("***** Private Law Case Worker Tasks *****")
    .exitBlockOnFail {
        feed(UserFeederPRLCourtAdmin)
      .feed(CWData)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(XuiHelper.Homepage)
          .exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
        //.exec(CourtAdmin_PRL_C100.CWOpenCase)
        .exec(CourtAdmin_PRL_C100.CWclickParties)
        .exec(CourtAdmin_PRL_C100.CWManageDocsUpload)
        .exec(CourtAdmin_PRL_C100.CWcaseFileView)
          .exec(CourtAdmin_PRL_C100.CWclickParties2)
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
* XUI Solicitor Private Law Scenario - FL401
 ===============================================================================================*/
  val PRLFL401SolicitorScenario = scenario("***** Private Law Solicitor FL401 Create Case *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .feed(UserFeederPRL)
        //Solicitor creates application
        .exec(Solicitor_PRL_FL401.CreatePrivateLawCase)
        .exec(Solicitor_PRL_FL401.TypeOfApplication)
        .exec(Solicitor_PRL_FL401.WithoutNoticeOrder)
        .exec(Solicitor_PRL_FL401.ApplicantDetails)
        .exec(Solicitor_PRL_FL401.RespondentDetails)
        .exec(Solicitor_PRL_FL401.ApplicantsFamily)
        .exec(Solicitor_PRL_FL401.Relationship)
        .exec(Solicitor_PRL_FL401.Behaviour)
        .exec(Solicitor_PRL_FL401.TheHome)
        .exec(Solicitor_PRL_FL401.UploadDocuments)
        .exec(Solicitor_PRL_FL401.ViewPDF)
        .exec(Solicitor_PRL_FL401.StatementOfTruth)
        .exec(XuiHelper.Logout)
        .feed(UserFeederPRLCourtAdmin)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
        //Case worker progress case
        .exec(CourtAdmin_PRL_FL401.CourtAdminCheckApplication)
        .exec(CourtAdmin_PRL_FL401.CourtAdminSendToGateKeeper)
        .exec(CourtAdmin_PRL_FL401.CourtAdminManageOrders)
        .exec(CourtAdmin_PRL_FL401.CourtAdminServiceApplication)
        .exec(CourtAdmin_PRL_FL401.CourtAdminListHearing)
        .exec(CourtAdmin_PRL_FL401.CourtAdminHearingsTab)
        .exec(XuiHelper.Logout)

    }

  /*===============================================================================================
* XUI Barrister Private Law C100 Scenario
 ===============================================================================================*/
	/*val PRLC100BarristerScenario = scenario("***** Private Law Barrister C100 Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederPRL)
				.feed(UserFeederPRLBarrister)
				.exec(session =>
					session.setAll(
					"appOrgID" -> session("orgID").as[String],
					"appBarristerUser" -> session("barristerUser").as[String],
					"appBarristerPassword" -> session("barristerPassword").as[String],
					"appBarristerFirm" -> session("barristerFirm").as[String],
					"appBarristerFirstName" -> session("barristerFirstName").as[String],
					"appBarristerLastName" -> session("barristerLastName").as[String],
					)
				)
				.feed(UserFeederPRLBarrister)
				.exec(session =>
					session.setAll(
						"defOrgID" -> session("orgID").as[String],
						"defBarristerUser" -> session("barristerUser").as[String],
						"defBarristerPassword" -> session("barristerPassword").as[String],
						"defBarristerFirm" -> session("barristerFirm").as[String],
						"defBarristerFirstName" -> session("barristerFirstName").as[String],
						"defBarristerLastName" -> session("barristerLastName").as[String],
					))
				.exec(getCaseC100(caseIdFeederC100))
				.exec(_.set("env", s"${env}")
				.set("caseType", "PRLAPPS"))
				.feed(UserFeederPRLCourtAdmin)
				.exec(_.set("currentDate", LocalDateTime.now.format(patternDate)))
        //Add Barrister for Applicant
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(CourtAdmin_PRL_C100.AddBarristerC100Applicant)
				.exec(XuiHelper.Logout)
        //Add Document
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{appBarristerUser}", "#{appBarristerPassword}"))
				.exec(Barrister_PRL_C100.AddDocumentC100Applicant)
				.exec(XuiHelper.Logout)
        //Add Barrister for Applicant
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(CourtAdmin_PRL_C100.AddBarristerC100Defendent)
				.exec(XuiHelper.Logout)
        //Add Document
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{defBarristerUser}", "#{defBarristerPassword}"))
				.exec(Barrister_PRL_C100.AddDocumentC100Respondent)
				.exec(XuiHelper.Logout)
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(CourtAdmin_PRL_C100.ServiceOfApplicationC100)
				.exec(CourtAdmin_PRL_C100.FinalDecisionC100)
				.exec(XuiHelper.Logout)
        //Remove Barrister
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(CourtAdmin_PRL_C100.RemoveBarristerC100Applicant)
				.exec(CourtAdmin_PRL_C100.RemoveBarristerC100Defendent)
				.exec(XuiHelper.Logout)
		}*/


	/*===============================================================================================
* XUI Barrister Private Law FL401 Scenario
 ===============================================================================================*/
	/*val PRLFL401BarristerScenario = scenario("***** Private Law Barrister FL401 Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederPRL)
				.feed(UserFeederPRLBarrister)
				.exec(session =>
					session.setAll(
						"appOrgID" -> session("orgID").as[String],
						"appBarristerUser" -> session("barristerUser").as[String],
						"appBarristerPassword" -> session("barristerPassword").as[String],
						"appBarristerFirm" -> session("barristerFirm").as[String],
						"appBarristerFirstName" -> session("barristerFirstName").as[String],
						"appBarristerLastName" -> session("barristerLastName").as[String]))
				.exec(_.set("currentDate", LocalDateTime.now.format(patternDate)))
				.exec(getCaseFL401(caseIdFeederFL401))
				.exec(_.set("env", s"${env}")
				.set("caseType", "PRLAPPS"))
				.feed(UserFeederPRLCourtAdmin)
        //Add Barrister
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(Solicitor_PRL_FL401.AddBarristerFL401Applicant)
				.exec(XuiHelper.Logout)
        //Draft order
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{appBarristerUser}", "#{appBarristerPassword}"))
				.exec(Barrister_PRL_FL401.DraftAnOrderFL401)
				.exec(XuiHelper.Logout)
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(CourtAdmin_PRL_FL401.ServiceOfApplicationFL401)
				.exec(CourtAdmin_PRL_FL401.FinalDecisionFL401)
				.exec(XuiHelper.Logout)
        //Remove Barrister
				.exec(XuiHelper.Homepage)
				.exec(XuiHelper.Login("#{userCourtAdmin}", "#{passwordCourtAdmin}"))
				.exec(Solicitor_PRL_FL401.RemoveBarristerFL401Applicant)
				.exec(XuiHelper.Logout)
		}*/

  /*===============================================================================================
  * XUI Legal Rep Bails Scenario
   ===============================================================================================*/
  val BailsScenario = scenario("***** Bails Create Application *****")
    .exitBlockOnFail {
      feed(UserFeederBails)
        .exec(_.set("env", s"${env}")
          .set("caseType", "Bail"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .exec(Solicitor_Bails.CreateBailApplication)
        .exec(Solicitor_Bails.SubmitBailApplication)
        .exec(XuiHelper.Logout)

        .feed(UserFeederBailsAdmin)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .exec(Solicitor_Bails.ConfirmLocation)
        .exec(Solicitor_Bails.ListCase)
        .exec(XuiHelper.Logout)

        .feed(UserFeederBailsHO)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .exec(Solicitor_Bails.UploadBailSummary)
        .exec(XuiHelper.Logout)

        .feed(UserFeederBailsJudge)
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .exec(Solicitor_Bails.RecordBailDecision)
        .exec(Solicitor_Bails.UploadSignedDecision)
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
  * XUI Solicitor Probate Scenario
   ===============================================================================================*/
  val ProbateSolicitorScenario = scenario("***** Probate Create Case *****")
    .exitBlockOnFail {
      feed(UserFeederProbate)
        .exec(_.set("env", s"${env}")
          .set("caseType", "GrantOfRepresentation"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .repeat(2) {
          exec(Solicitor_Probate.CreateProbateCase)
            .exec(Solicitor_Probate.AddDeceasedDetails)
            .exec(Solicitor_Probate.AddApplicationDetails)
            .exec(Solicitor_Probate.ReviewAndSubmitApplication)
        }
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
  * XUI Solicitor IAC Scenario
   ===============================================================================================*/
  val ImmigrationAndAsylumSolicitorScenario = scenario("***** IAC Create Case *****")
    .exitBlockOnFail {
      feed(UserFeederIAC)
        .exec(_.set("env", s"${env}")
          .set("caseType", "Asylum"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .repeat(2) {
          exec(Solicitor_IAC.CreateIACCase)
          // .exec(Solicitor_IAC.shareacase) //Temp removed as the way to share a case is now done through the case list
        }
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
  * XUI Solicitor NFD Scenario (Sole Application)
   ===============================================================================================*/
  val NoFaultDivorceSolicitorSoleScenario = scenario("***** NFD Create Case (Sole) *****")
    .exitBlockOnFail {
      //feed two rows of data - applicant1's solicitor and applicant2's solicitor
      feed(UserFeederNFD, 2)

        /*
        the below code (first 4 lines) is required since Gatling 3.8, as the multi-line feeder above no longer
        generates individual session variables, but now produces arrays - see https://github.com/gatling/gatling/issues/4226
        Once set in the session as sequences, the session variables can be referenced:
        e.g. #{users(0)}, #{users(1)}, etc using the Gatling DSL
        or session("users").as[Seq[String]].apply(0) without the DSL
         */
        .exec { session =>
          session
            .set("env", s"${env}")
            .set("caseType", "NFD")
            .set("nfdCaseType", "sole")
            .set("NFDLabelsInitialised", nfdSoleLabelsInitialised) //sets the initialised labels for JSON bodies
            .set("NFDLabelsPopulated", nfdSoleLabelsPopulated) //sets the populated labels for JSON bodies
        }

        //Solicitor 1 - Divorce Application
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.CreateNFDCase)
        .exec(Solicitor_NFD.SignAndSubmitSole)
        .exec(XuiHelper.Logout)
        //Caseworker - Issue Application
        .exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-issue-application", "bodies/nfd/CWIssueApplication.json"))
        //Update the case in CCD to assign it to the second solicitor
        .exec(CCDAPI.AssignCase)
        //Solicitor 2 - Respond to Divorce Application
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(1)}", "#{password(1)}"))
        .exec(Solicitor_NFD.RespondToNFDCase)
        .exec(XuiHelper.Logout)
        //Caseworker - Mark the Case as Awaiting Conditional Order (to bypass 20-week holding)
        .exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-held-case", "bodies/nfd/CWAwaitingConditionalOrder.json"))
        //Solicitor 1 - Apply for Conditional Order
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.ApplyForCOSole)
        .exec(Solicitor_NFD.SubmitCO)
        .exec(XuiHelper.Logout)
        //Legal Advisor - Grant Conditional Order
        .exec(CCDAPI.CreateEvent("Legal", "DIVORCE", "NFD", "legal-advisor-make-decision", "bodies/nfd/LAMakeDecision.json"))
        //Caseworker - Make Eligible for Final Order
        .exec(
          //link with bulk case
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-link-with-bulk-case", "bodies/nfd/CWLinkWithBulkCase.json"),
          //set case hearing and decision dates to a date in the past
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-update-case-court-hearing", "bodies/nfd/CWUpdateCaseWithCourtHearing.json"),
          //set judge details, CO granted and issued dates in the past
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-amend-case", "bodies/nfd/CWSetCODetails.json"),
          //pronounce case
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-pronounce-case", "bodies/nfd/CWPronounceCase.json"),
          //set final order eligibility dates
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-amend-case", "bodies/nfd/CWSetFOEligibilityDates.json"),
          //set case as awaiting final order
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-case-awaiting-final-order", "bodies/nfd/CWAwaitingFinalOrder.json"))
        //Solicitor 1 - Apply for Final Order
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.ApplyForFO)
        .exec(XuiHelper.Logout)
        //Caseworker - Grant Final Order
        .exec(
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-grant-final-order", "bodies/nfd/CWGrantFinalOrder.json"))
    }

  /*.exec {
    session =>
      println(session)
      session
  }*/

  /*===============================================================================================
  * XUI Solicitor NFD Scenario (Joint Application)
   ===============================================================================================*/
  val NoFaultDivorceSolicitorJointScenario = scenario("***** NFD Create Case (Joint) *****")
    .exitBlockOnFail {
      //feed two rows of data - applicant1's solicitor and applicant2's solicitor
      feed(UserFeederNFD, 2)

        /*
        the below code (first 4 lines) is required since Gatling 3.8, as the multi-line feeder above no longer
        generates individual session variables, but now produces arrays - see https://github.com/gatling/gatling/issues/4226
        Once set in the session as sequences, the session variables can be referenced:
        e.g. #{users(0)}, #{users(1)}, etc using the Gatling DSL
        or session("users").as[Seq[String]].apply(0) without the DSL
         */
        .exec { session =>
          session
            .set("env", s"${env}")
            .set("caseType", "NFD")
            .set("nfdCaseType", "joint")
            .set("NFDLabelsInitialised", nfdJointLabelsInitialised) //sets the initialised labels for JSON bodies
            .set("NFDLabelsPopulated", nfdJointLabelsPopulated) //sets the populated labels for JSON bodies
        }

        //Solicitor 1 - Divorce Application
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.CreateNFDCase)
        .exec(Solicitor_NFD.JointInviteApplicant2)
        .exec(XuiHelper.Logout)
        //Update the case in CCD to assign it to the second solicitor
        .exec(CCDAPI.AssignCase)
        //Solicitor 2 - Confirm Divorce Application
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(1)}", "#{password(1)}"))
        .exec(Solicitor_NFD.SubmitJointApplication)
        .exec(XuiHelper.Logout)
        //Solicitor 1 - Submit Application
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.SignAndSubmitJoint)
        .exec(XuiHelper.Logout)
        //Caseworker - Issue Application
        .exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-issue-application", "bodies/nfd/CWIssueApplication.json"))
        //Caseworker - Mark the Case as Awaiting Conditional Order (to bypass 20-week holding)
        .exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-held-case", "bodies/nfd/CWAwaitingConditionalOrder.json"))
        //Solicitor 1 - Apply for Conditional Order
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.ApplyForCOJointApplicant1)
        .exec(Solicitor_NFD.SubmitCO)
        .exec(XuiHelper.Logout)
        //Solicitor 2 - Apply for Conditional Order
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(1)}", "#{password(1)}"))
        .exec(Solicitor_NFD.ApplyForCOJointApplicant2)
        .exec(Solicitor_NFD.SubmitCOJoint)
        .exec(XuiHelper.Logout)
        //Legal Advisor - Grant Conditional Order
        .exec(CCDAPI.CreateEvent("Legal", "DIVORCE", "NFD", "legal-advisor-make-decision", "bodies/nfd/LAMakeDecision.json"))
        //Caseworker - Make Eligible for Final Order
        .exec(
          //link with bulk case
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-link-with-bulk-case", "bodies/nfd/CWLinkWithBulkCase.json"),
          //set case hearing and decision dates to a date in the past
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-update-case-court-hearing", "bodies/nfd/CWUpdateCaseWithCourtHearing.json"),
          //set judge details, CO granted and issued dates in the past
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-amend-case", "bodies/nfd/CWSetCODetails.json"),
          //pronounce case
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-pronounce-case", "bodies/nfd/CWPronounceCase.json"),
          //set final order eligibility dates
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-amend-case", "bodies/nfd/CWSetFOEligibilityDates.json"),
          //set case as awaiting final order
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-case-awaiting-final-order", "bodies/nfd/CWAwaitingFinalOrder.json"))
        //Solicitor 1 - Apply for Final Order
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(0)}", "#{password(0)}"))
        .exec(Solicitor_NFD.ApplyForFO)
        .exec(XuiHelper.Logout)
        //Solicitor 2 - Apply for Final Order
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user(1)}", "#{password(1)}"))
        .exec(Solicitor_NFD.ApplyForFOJoint)
        .exec(XuiHelper.Logout)
        //Caseworker - Grant Final Order
        .exec(
          CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-grant-final-order", "bodies/nfd/CWGrantFinalOrder.json"))
    }

  /*.exec {
    session =>
      println(session)
      session
  }*/

  /*===============================================================================================
  * XUI Solicitor Financial Remedy (FR) Scenario
   ===============================================================================================*/
  val FinancialRemedySolicitorScenario = scenario("***** FR Create Case *****")
    .exitBlockOnFail {
      feed(UserFeederFR)
        .exec(_.set("env", s"${env}")
          .set("caseType", "FinancialRemedyMVP2"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .repeat(2) {
          exec(Solicitor_FR.CreateFRCase)
        }
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
  * XUI Solicitor Family Public Law (FPL) Scenario
   ===============================================================================================*/
  val FamilyPublicLawSolicitorScenario = scenario("***** FPL Create Case *****")
    .exitBlockOnFail {
      feed(UserFeederFPL)
        .exec(_.set("env", s"${env}")
          .set("caseType", "CARE_SUPERVISION_EPO"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .exec(Solicitor_FPL.CreateFPLCase)
        .exec(Solicitor_FPL.fplOrdersAndDirections)
        .exec(Solicitor_FPL.fplHearingUrgency)
        .exec(Solicitor_FPL.fplGrounds)
        .exec(Solicitor_FPL.fplLocalAuthority)
        .exec(Solicitor_FPL.fplChildDetails)
        .exec(Solicitor_FPL.fplRespondentDetails)
        .exec(Solicitor_FPL.fplAllocationProposal)
        .exec(Solicitor_FPL.fplSubmitApplication)
        .exec(Solicitor_FPL.fplReturnToCase)
        //.exec(Solicitor_FPL.QueryManagement) //Temporarily removing QM until FPL is onboarded in XUI master
        .exec(XuiHelper.Logout)
      //.feed(UserFeederCTSC)
      //.exec(Homepage.XUIHomePage)
      //.exec(Login.XUILogin)
      //.exec(Solicitor_FPL.RespondToQueryManagement)
      //.exec(XuiHelper.Logout)
    }

  /*===============================================================================================
  * XUI Caseworker - Search & View Case Scenario
   ===============================================================================================*/
  val CaseworkerScenario = scenario("***** Caseworker Journey ******")
    .exitBlockOnFail {
      feed(CaseworkerUserFeeder)
        //TODO: UPDATE caseType with something more dynamic
        .exec(_.set("env", s"${env}")
          .set("caseType", "NFD"))
        .exec(XuiHelper.Homepage)
        .exec(XuiHelper.Login("#{user}", "#{password}"))
        .exec(Caseworker_Navigation.ApplyFilter)
        .exec(Caseworker_Navigation.SortByLastModifiedDate)
        .exec(Caseworker_Navigation.LoadPage2)
        //Only continue with the case activities if results were returned
        .doIf(session => session("numberOfResults").as[Int] > 0) {
          exec(Caseworker_Navigation.SearchByCaseNumber)
            .exec(Caseworker_Navigation.ViewCase)
            // .exec(Caseworker_Navigation.NavigateTabs) //Removing as clicking tabs no longer initiates calls
            .exec(Caseworker_Navigation.ViewDocument)
        }
        .exec(Caseworker_Navigation.LoadCaseList)
        .exec(XuiHelper.Logout)
    }

  /*===============================================================================================
  * Simulation Configuration
   ===============================================================================================*/

  def simulationProfile(simulationType: String, userPerHourRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    val userPerSecRate = userPerHourRate / 3600
    simulationType match {
      case "perftest" =>
        if (debugMode == "off") {
          Seq(
            rampUsersPerSec(0.00) to (userPerSecRate) during (rampUpDurationMins.minutes),
            constantUsersPerSec(userPerSecRate) during (testDurationMins.minutes),
            rampUsersPerSec(userPerSecRate) to (0.00) during (rampDownDurationMins.minutes)
          )
        }
        else {
          Seq(atOnceUsers(1))
        }
      case "pipeline" =>
        Seq(rampUsers(numberOfPipelineUsers.toInt) during (2.minutes))
      case _ =>
        Seq(nothingFor(0))
    }
  }

  //defines the test assertions, based on the test type
  def assertions(simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" | "pipeline" => //currently using the same assertions for a performance test and the pipeline
        if (debugMode == "off") {
          Seq(global.successfulRequests.percent.gte(95),
            details("XUI_PRL_C100_700_SubmitAndPayNow").successfulRequests.percent.gte(80),
            details("XUI_PRL_FL401_490_SOTSubmit").successfulRequests.percent.gte(80),
            details("XUI_Bails_770_Upload_Signed_Notice_Submit").successfulRequests.percent.gte(80),
            details("XUI_Probate_330_ViewCase").successfulRequests.percent.gte(80),
            details("XUI_IAC_280_005_AppealDeclarationSubmitted").successfulRequests.percent.gte(80),
            details("XUI_000_CCDEvent-system-progress-case-awaiting-final-order").successfulRequests.percent.gte(80), //NFD Sole
            details("XUI_000_CCDEvent-system-progress-held-case").successfulRequests.percent.gte(80), //NFD Joint
            details("XUI_FR_170_SubmitApplication").successfulRequests.percent.gte(80),
            details("XUI_FPL_330_ReturnToCase").successfulRequests.percent.gte(80),
            details("XUI_Caseworker_100_CaseList").successfulRequests.percent.gte(80))
        }
        else {
          Seq(global.successfulRequests.percent.is(100))
        }
      case _ =>
        Seq()
    }
  }

  setUp(
    //PRLC100AddLAScenario.inject(simulationProfile(testType, 100, 100)).pauses(pauseOption),
    ///PRLC100SolicitorScenario.inject(simulationProfile(testType, 1, 1)).pauses(pauseOption)
    //PRLC100LAScenario.inject(simulationProfile(testType, 1, 1)).pauses(pauseOption)
    //PRLC100CaseWorkerScenario.inject(simulationProfile(testType, prlC100LATargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    //PRLC100CaseWorkerScenario.inject(simulationProfile(testType, prlC100LATargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    PRLC100AddLAScenario.inject(simulationProfile(testType, prlC100LATargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
      PRLC100LAScenario.inject(simulationProfile(testType, prlC100LATargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLC100SolicitorScenario.inject(simulationProfile(testType, prlC100TargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLFL401SolicitorScenario.inject(simulationProfile(testType, prlFL401TargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    /*PRLC100BarristerScenario.inject(simulationProfile(testType, prlC100BarristerTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    PRLFL401BarristerScenario.inject(simulationProfile(testType, prlFL401BarristerTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    BailsScenario.inject(simulationProfile(testType, bailsTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    ProbateSolicitorScenario.inject(simulationProfile(testType, probateTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    ImmigrationAndAsylumSolicitorScenario.inject(simulationProfile(testType, iacTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    NoFaultDivorceSolicitorSoleScenario.inject(simulationProfile(testType, nfdSoleTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    NoFaultDivorceSolicitorJointScenario.inject(simulationProfile(testType, nfdJointTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    FinancialRemedySolicitorScenario.inject(simulationProfile(testType, frTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    FamilyPublicLawSolicitorScenario.inject(simulationProfile(testType, fplTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
    CaseworkerScenario.inject(simulationProfile(testType, caseworkerTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),*/

  ).protocols(httpProtocol)
    .assertions(assertions(testType))
    .maxDuration(75.minutes)

}
