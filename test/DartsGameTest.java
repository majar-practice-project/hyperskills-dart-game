import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dartsgame.DartsGameApplication;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.request.HttpRequest;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hyperskill.hstest.common.JsonUtils.getJson;
import static org.hyperskill.hstest.mocks.web.constants.Headers.AUTHORIZATION;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;

class TestHint {
  private final String apiPath;
  private final String requestBody;
  private final String message;

  public TestHint(String apiPath, String requestBody, String message) {
    this.apiPath = apiPath;
    this.requestBody = requestBody;
    this.message = message;
  }

  @Override
  public String toString() {
    return "Test case\n" +
            "Testing api: '" + apiPath + '\'' + "\n" +
            (requestBody.length() > 0 ? "request: '" + requestBody + '\'' + "\n" : "") +
            "Expectations: '" + message + "'" + "\n" +
            "-----";
  }
}

public class DartsGameTest extends SpringTest {

  private final String apiCreate = "/api/game/create";
  private final String apiList = "/api/game/list";
  private final String apiJoin = "/api/game/join";
  private final String apiStatus = "/api/game/status";
  private final String apiThrows = "/api/game/throws";
  private final String tokenApi = "/oauth/token";
  private final String historyApi = "/api/history/";
  private final String cancelApi = "/api/game/cancel";
  private final String revertApi = "/api/game/revert";

  private final List<Integer> gameIds = new ArrayList();

  private String bearerToken = "";
  private final String clientId = "hyperdarts";
  private final String clientSecret = "secret";

  private final String ivanHoe = """
      {
         "name": "Ivan",
         "lastname": "Hoe",
         "email": "ivanhoe@acme.com",
         "password": "oMoa3VvqnLxW"
      }""";

  private final String robinHood = """
      {
         "name": "Robin",
         "lastname": "Hood",
         "email": "robinhood@acme.com",
         "password": "ai0y9bMvyF6G"
      }""";

  private final String wilhelmTell = """
      {
         "name": "Wilhelm",
         "lastname": "Tell",
         "email": "wilhelmtell@acme.com",
         "password": "bv0y9bMvyF7E"
      }""";

  private final String wrongUser = """
      {
         "name": "Bobin",
         "lastname": "Hood",
         "email": "bobinhood@acme.com",
         "password": "be0y9bMvyF6G"
      }""";

  private final String referee = """
      {
         "name": "Judge",
         "lastname": "Dredd",
         "email": "judgedredd@acme.com",
         "password": "iAmALaw100500"
      }""";

  private final String jwtSigningKey = """
      -----BEGIN PUBLIC KEY-----
      MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQ+7yKlJGuvYtf1soMsJjkQJGA
      Xe90QAxqppycf+3JT5ehnvvWtwS8ef+UsqrNa5Rc9tyyHjP7ZXRN145SlRTZzc0d
      03Ez10OfAEVdhGACgRxS5s+GZVtdJuVcje3Luq3VIvZ8mV/P4eRcV3yVKDwQEenM
      uL6Mh6JLH48KxgbNRQIDAQAB
      -----END PUBLIC KEY-----""";

  // for create API
  private final String wrongScore = """
      {
         "targetScore": 601
      }""";
  private final String wrongScoreAnswer = """
      {
         "result": "Wrong target score!"
      }""";
  private final String correctScore = """
      {
         "targetScore": 501
      }""";
  private final String correctScore101 = """
      {
         "targetScore": 101
      }""";
  private final String gameExistAnswer = """
      {
         "result": "You have an unfinished game!"
      }""";
  private final String answerWT = """
      {
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      }""";
  private final String answerRH = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 501,
         "playerTwoScores": 501,
         "turn": "ivanhoe@acme.com"
      }""";

  // for status API
  private final String emptyAnswer = "{}";
  private final String statusAnswer = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 501,
         "playerTwoScores": 501,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String statusAnswer2 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "ivanhoe@acme.com wins!",
         "playerOneScores": 0,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String statusAnswer3 = """
      {
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "started",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      }""";

  private final String statusAnswer4 = """
      {
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "robinhood@acme.com"
      }""";

  private final String statusAnswer5 = """
      {
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "robinhood@acme.com wins!",
         "playerOneScores": 101,
         "playerTwoScores": 0,
         "turn": "robinhood@acme.com"
      }""";

  private final String statusAnswer6 = """
      {
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "robinhood@acme.com wins!",
         "playerOneScores": 101,
         "playerTwoScores": 0,
         "turn": "robinhood@acme.com"
      }""";

  private final String statusAnswer7 = """
      {
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 501,
        "turn" : "robinhood@acme.com"
      }""";

  private final String statusAnswer8 = """
      {
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 321,
        "turn" : "ivanhoe@acme.com"
      }""";


  // for list API
  private final String emptyArray = "[]";
  private final String listAnswer = """
      [{
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      },
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 501,
         "playerTwoScores": 501,
         "turn": "ivanhoe@acme.com"
      }]"""
          ;

  private final String listAnswer2 = """
      [{
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      },
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "started",
         "playerOneScores": 501,
         "playerTwoScores": 501,
         "turn": "ivanhoe@acme.com"
      }]"""
          ;

  private final String listAnswer3 = """
      [{
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "",
         "gameStatus": "created",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      },
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "ivanhoe@acme.com wins!",
         "playerOneScores": 0,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }]"""
          ;

  private final String listAnswer4 = """
      [{
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "started",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      },
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "ivanhoe@acme.com wins!",
         "playerOneScores": 0,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }]"""
          ;

  // for join API
  private final String gameNotFound = """
      {
         "result": "Game not found!"
      }""";

  private final String unFinishedGame = """
      {
         "result": "You have an unfinished game!"
      }""";

  private final String gameInProgress = """
      {
         "result": "You can't join the game!"
      }""";

  private final String autoGame = """
      {
         "result": "You can't play alone!"
      }""";

  private final String joinAnswerIH = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "started",
         "playerOneScores": 501,
         "playerTwoScores": 501,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String joinAnswerRH = """
      {
         "playerOne": "wilhelmtell@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "started",
         "playerOneScores": 101,
         "playerTwoScores": 101,
         "turn": "wilhelmtell@acme.com"
      }""";

  private final String joinAnswerRH1 = """
      {
        "playerOne": "ivanhoe@acme.com",
        "playerTwo": "robinhood@acme.com",
        "gameStatus": "started",
        "playerOneScores": 501,
        "playerTwoScores": 501,
        "turn": "ivanhoe@acme.com"
      }""";

  // throws api
  private final String correctThrows1 = """
      {
         "first": "1:1",
         "second": "2:2",
         "third": "3:3"
      }""";

  private final String correctThrows2 = """
      {
         "first": "3:20",
         "second": "3:20",
         "third": "3:20"
      }""";

  private final String correctThrows3 = """
      {
         "first": "2:25",
         "second": "3:20",
         "third": "3:20"
      }""";

  private final String correctThrows4 = """
      {
         "first": "2:25",
         "second": "2:25",
         "third": "3:9"
      }""";

  private final String correctThrows5 = """
      {
         "first": "3:17",
         "second": "3:20",
         "third": "3:10"
      }""";

  private final String correctThrows6 = """
      {
         "first": "2:10",
         "second": "none",
         "third": "none"
      }""";

  private final String wrongThrows1 = """
      {
         "first": "3:25",
         "second": "1:10",
         "third": "2:20"
      }""";

  private final String wrongThrows2 = """
      {
         "first": "2:22",
         "second": "1:10",
         "third": "2:20"
      }""";

  private final String wrongThrows3 = """
      {
         "first": "0:9",
         "second": "1:10",
         "third": "2:20"
      }""";

  private final String wrongThrows4 = """
      {
         "first": "1:26",
         "second": "1:10",
         "third": "2:20"
      }""";

  private final String wrongThrows5 = """
      {
         "first": "3:99",
         "second": "1:10",
         "third": "2:20"
      }""";

  private final String wrongBustThrows1 = """
      {
         "first": "3:20",
         "second": "3:20",
         "third": "3:20"
      }""";

  private final String CorrectBustThrows1 = """
      {
         "first": "3:20",
         "second": "3:20",
         "third": "none"
      }""";

  private final String winThrowsRH = """
      {
         "first": "3:17",
         "second": "2:25",
         "third": "none"
      }""";


  private final String wrongTurn = """
      {
         "result": "Wrong turn!"
      }""";

  private final String wrongThrowsAnswer = """
      {
         "result": "Wrong throws!"
      }""";

  private final String noGames = """
      {
         "result": "There are no games available!"
      }""";

  private final String throwsAnswer1 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 487,
         "playerTwoScores": 501,
         "turn": "robinhood@acme.com"
      }""";

  private final String throwsAnswer2 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 487,
         "playerTwoScores": 321,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String throwsAnswer3 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 317,
         "playerTwoScores": 321,
         "turn": "robinhood@acme.com"
      }""";


  private final String throwsAnswer4 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 317,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String throwsAnswer5 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 147,
         "playerTwoScores": 141,
         "turn": "robinhood@acme.com"
      }""";

  private final String throwsAnswer6 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 147,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String throwsAnswer7 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 20,
         "playerTwoScores": 141,
         "turn": "robinhood@acme.com"
      }""";

  private final String throwsAnswer8 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "playing",
         "playerOneScores": 20,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String throwsAnswer9 = """
      {
         "playerOne": "ivanhoe@acme.com",
         "playerTwo": "robinhood@acme.com",
         "gameStatus": "ivanhoe@acme.com wins!",
         "playerOneScores": 0,
         "playerTwoScores": 141,
         "turn": "ivanhoe@acme.com"
      }""";

  private final String historyNotFound = """
      {
         "result": "Game not found!"
      }""";

  private final String historyBad = """
      {
         "result": "Wrong request!"
      }""";


  private final String historyAnswer1 = """
      [ {
        "move" : 0,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "started",
        "playerOneScores" : 501,
        "playerTwoScores" : 501,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 1,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 501,
        "turn" : "robinhood@acme.com"
      }, {
        "move" : 2,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 321,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 3,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 317,
        "playerTwoScores" : 321,
        "turn" : "robinhood@acme.com"
      }, {
        "move" : 4,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 317,
        "playerTwoScores" : 141,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 5,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 147,
        "playerTwoScores" : 141,
        "turn" : "robinhood@acme.com"
      }, {
        "move" : 6,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 147,
        "playerTwoScores" : 141,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 7,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 20,
        "playerTwoScores" : 141,
        "turn" : "robinhood@acme.com"
      }, {
        "move" : 8,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 20,
        "playerTwoScores" : 141,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 9,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "ivanhoe@acme.com wins!",
        "playerOneScores" : 0,
        "playerTwoScores" : 141,
        "turn" : "ivanhoe@acme.com"
      } ]""";

  private final String historyAnswer2 = """
      [ {
        "move" : 0,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "started",
        "playerOneScores" : 501,
        "playerTwoScores" : 501,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 1,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 501,
        "turn" : "robinhood@acme.com"
      }, {
        "move" : 2,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 321,
        "turn" : "ivanhoe@acme.com"
      } ]""";

  private final String historyAnswer3 = """
      [ {
        "move" : 0,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "started",
        "playerOneScores" : 501,
        "playerTwoScores" : 501,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 1,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 501,
        "turn" : "robinhood@acme.com"
      }, {
        "move" : 2,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 321,
        "turn" : "ivanhoe@acme.com"
      } ]
      """;

  private final String historyAnswer4 = """
      [ {
        "move" : 0,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "started",
        "playerOneScores" : 501,
        "playerTwoScores" : 501,
        "turn" : "ivanhoe@acme.com"
      }, {
        "move" : 1,
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 501,
        "turn" : "robinhood@acme.com"
      } ]""";


  private final String cancelAnswer1 = """
      {
         "result": "The game is already over!"
      }""";

  private final String cancelAnswer2 = """
      {
        "playerOne" : "wilhelmtell@acme.com",
        "playerTwo" : "",
        "gameStatus" : "Nobody wins!",
        "playerOneScores" : 101,
        "playerTwoScores" : 101,
        "turn" : "wilhelmtell@acme.com"
      }""";

  private final String cancelAnswer3 = """
      {
         "result": "Game not found!"
      }""";

  private final String cancelAnswer4 = """
      {
         "result": "Wrong status!"
      }""";

  private final String revertAnswer1 = """
      {
        "playerOne" : "ivanhoe@acme.com",
        "playerTwo" : "robinhood@acme.com",
        "gameStatus" : "playing",
        "playerOneScores" : 487,
        "playerTwoScores" : 501,
        "turn" : "robinhood@acme.com"
      }""";

  private final String revertNotFound = """
      {
         "result": "Move not found!"
      }""";

  private final String revertLast = """
      {
         "result": "There is nothing to revert!"
      }""";

  private final String revertOver = """
      {
         "result": "The game is over!"
      }""";



  public DartsGameTest() {
    super(DartsGameApplication.class, "../service_db.mv.db");
  }

  /**
   * Method for testing api response
   *
   * @param token string representation of bearer token (String)
   * @param body request body (String)
   * @param status expected response status (int)
   * @param api testing api (String)
   * @param method method for api (String)
   * @return response (HttpResponse)
   */
  private HttpResponse checkResponseStatus(String token, String body,
                                           int status, String api, String method) {
    HttpRequest request = switch (method) {
      case "GET" -> get(api);
      case "POST" -> post(api, body);
      case "PUT" -> put(api, body);
      case "DELETE" -> delete(api);
      default -> null;
    };

    if (!token.equals("")) {
      String headerValue = "Bearer " + token;
      assert request != null;
      request = request.addHeader(AUTHORIZATION, headerValue);
    }
    HttpResponse response = request.send();

    if (response.getStatusCode() != status) {
      throw new WrongAnswer(method + " " + api  + " should respond with "
              + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
              + "Response body:\n" + response.getContent() + "\n");
    }
    return response;
  }

  private CheckResult testApi(String api, String body, String method,
                              int status, String token, String answer, TestHint hint) {

    System.out.println(hint.toString());

    HttpResponse response = checkResponseStatus(token, body, status, api, method);

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("status", answer));

    }
    return CheckResult.correct();
  }

  private CheckResult getToken(String user, String scope, int status, TestHint hint) {

    System.out.println(hint.toString());

    JsonObject userJson = getJson(user).getAsJsonObject();
    String password = userJson.get("password").getAsString();
    String login = userJson.get("email").getAsString().toLowerCase();

    Map<String, String> urlParams = Map.of("grant_type", "password", "username", login,
            "password", password, "scope", scope);
    System.out.println("Request params:\n" +
            "Client ID: " + clientId + "\n" +
            "Client password: " + clientSecret + "\n" +
            "User: " + login + "\n" +
            "User password: " + password + "\n" +
            "Scope: " + scope);

    HttpResponse response = post("/oauth/token", urlParams)
            .basicAuth(clientId, clientSecret).send();


    if (response.getStatusCode() != status) {
      return CheckResult.wrong("POST " + tokenApi + " should respond with "
              + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
              + response.getStatusCode() + "\n"
              + "Response body:\n" + response.getContent());
    }
    String r = response.getContent();

    if (!r.endsWith("}")) {
      r = response.getContent() + "}";
    }
    JsonObject resp = getJson(r).getAsJsonObject();
    bearerToken = resp.get("access_token").getAsString();
    return CheckResult.correct();
  }

  private CheckResult checkToken(String user, String[] scope, String role, TestHint hint) {

    System.out.println(hint.toString());

    JsonObject userJson = getJson(user).getAsJsonObject();
    String login = userJson.get("email").getAsString().toLowerCase();
    Jwt decodedToken;


    try {
      decodedToken = JwtHelper.decode(bearerToken);
      System.out.println("Checking token:\n" +
              decodedToken);
    } catch (Exception e) {
      return CheckResult.wrong("Wrong token format!");
    }


    try {
      JwtHelper.decodeAndVerify(bearerToken, new RsaVerifier(jwtSigningKey));
    } catch (Exception e) {
      return CheckResult.wrong("Wrong token signature!");
    }

    expect(decodedToken.getClaims()).asJson().check(
            isObject()
                    .value("client_id", "hyperdarts")
                    .value("user_name", login)
                    .value("scope", scope)
                    .value("exp", isInteger())
                    .value("authorities", new String[] {role})
                    .anyOtherValues());

    return CheckResult.correct();
  }


  private CheckResult testTokenApi(String user, String clientId, String clientSecret, int status, TestHint hint) {

    System.out.println(hint.toString());

    JsonObject userJson = getJson(user).getAsJsonObject();
    String password = userJson.get("password").getAsString();
    String login = userJson.get("email").getAsString().toLowerCase();

    Map<String, String> urlParams = Map.of("grant_type", "password", "username", login, "password", password);

    HttpResponse response = post(tokenApi, urlParams)
            .basicAuth(clientId, clientSecret).send();


    if (response.getStatusCode() != status) {
      return CheckResult.wrong("POST " + tokenApi + " should respond with "
              + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
              + response.getStatusCode() + "\n"
              + "Response body:\n" + response.getContent());
    }
    return CheckResult.correct();
  }

  private CheckResult testCreateApi(String body, int status, String token, String answer,
                                    TestHint hint) {

    System.out.println(hint.toString());

    HttpResponse response = checkResponseStatus(token, body, status, "/api/game/create", "POST");

    JsonObject answerJson = getJson(answer).getAsJsonObject();

    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("gameId", isInteger())
                      .value("playerOne", answerJson.get("playerOne").getAsString())
                      .value("playerTwo", answerJson.get("playerTwo").getAsString())
                      .value("gameStatus", answerJson.get("gameStatus").getAsString())
                      .value("playerOneScores", answerJson.get("playerOneScores").getAsInt())
                      .value("playerTwoScores", answerJson.get("playerTwoScores").getAsInt())
                      .value("turn", answerJson.get("turn").getAsString()));

      gameIds.add(getJson(response.getContent()).getAsJsonObject().get("gameId").getAsInt());

    } else {

      // Check JSON in response
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("result", answerJson.get("result").getAsString()));
    }

    return CheckResult.correct();
  }

  private CheckResult testStatusApi(int status, String token, String answer, TestHint hint) {

    System.out.println(hint.toString());

    HttpResponse response = checkResponseStatus(token, "", status, "/api/game/status", "GET");

    JsonObject answerJson = getJson(answer).getAsJsonObject();

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("gameId", isInteger())
                      .value("playerOne", answerJson.get("playerOne").getAsString())
                      .value("playerTwo", answerJson.get("playerTwo").getAsString())
                      .value("gameStatus", answerJson.get("gameStatus").getAsString())
                      .value("playerOneScores", answerJson.get("playerOneScores").getAsInt())
                      .value("playerTwoScores", answerJson.get("playerTwoScores").getAsInt())
                      .value("turn", answerJson.get("turn").getAsString()));

    } else {
      expect(response.getContent()).asJson().check(
              isObject());
    }
    return CheckResult.correct();
  }

  private CheckResult testJoinApi(int status, String token, String answer, int gameId, TestHint hint) {

    System.out.println(hint.toString());

    HttpResponse response = checkResponseStatus(token, "", status, "/api/game/join" + "/" + gameId, "GET");

    JsonObject answerJson = getJson(answer).getAsJsonObject();

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("gameId", gameId)
                      .value("playerOne", answerJson.get("playerOne").getAsString())
                      .value("playerTwo", answerJson.get("playerTwo").getAsString())
                      .value("gameStatus", answerJson.get("gameStatus").getAsString())
                      .value("playerOneScores", answerJson.get("playerOneScores").getAsInt())
                      .value("playerTwoScores", answerJson.get("playerTwoScores").getAsInt())
                      .value("turn", answerJson.get("turn").getAsString()));

    } else {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("result", answerJson.get("result").getAsString()));
    }
    return CheckResult.correct();
  }


  private CheckResult testListApi(int status, String token, String answer) {

    HttpResponse response = checkResponseStatus(token, "", status, "/api/game/list", "GET");

    JsonArray correctJson = getJson(answer).getAsJsonArray();
    JsonArray responseJson;
    try {
      responseJson = getJson(response.getContent()).getAsJsonArray();
    } catch (Exception e) {
      throw new WrongAnswer("Must be array of JSON's in answer");
    }

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      for (int i = 0; i < responseJson.size(); i++) {
        expect(responseJson.get(i).getAsJsonObject().toString()).asJson()
                .check(isObject()
                        .value("gameId", isInteger())
                        .value("playerOne", correctJson.get(i).getAsJsonObject().get("playerOne").getAsString())
                        .value("playerTwo", correctJson.get(i).getAsJsonObject().get("playerTwo").getAsString())
                        .value("gameStatus", correctJson.get(i).getAsJsonObject().get("gameStatus").getAsString())
                        .value("playerOneScores", correctJson.get(i).getAsJsonObject().get("playerOneScores").getAsInt())
                        .value("playerTwoScores", correctJson.get(i).getAsJsonObject().get("playerTwoScores").getAsInt())
                        .value("turn", correctJson.get(i).getAsJsonObject().get("turn").getAsString()));
      }
    }  else {
      expect(response.getContent()).asJson().check(
              isArray());
    }
    return CheckResult.correct();
  }

  private CheckResult testThrowsApi(String body, int status, String token, String answer,
                                    TestHint hint) {

    System.out.println(hint.toString());

    HttpResponse response = checkResponseStatus(token, body, status, "/api/game/throws", "POST");

    JsonObject answerJson = getJson(answer).getAsJsonObject();

    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("gameId", isInteger())
                      .value("playerOne", answerJson.get("playerOne").getAsString())
                      .value("playerTwo", answerJson.get("playerTwo").getAsString())
                      .value("gameStatus", answerJson.get("gameStatus").getAsString())
                      .value("playerOneScores", answerJson.get("playerOneScores").getAsInt())
                      .value("playerTwoScores", answerJson.get("playerTwoScores").getAsInt())
                      .value("turn", answerJson.get("turn").getAsString()));
    } else {

      // Check JSON in response
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("result", answerJson.get("result").getAsString()));
    }

    return CheckResult.correct();
  }

  private CheckResult testCancelApi(Integer gameId, String body, int status, String token, String answer,
                                    TestHint hint) {

    System.out.println(hint.toString());

    JsonObject jsonBody = new JsonObject();
    jsonBody.addProperty("gameId", gameId);
    jsonBody.addProperty("status", body);

    HttpResponse response = checkResponseStatus(token, jsonBody.toString(), status, "/api/game/cancel", "PUT");

    JsonObject answerJson = getJson(answer).getAsJsonObject();

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("gameId", isInteger())
                      .value("playerOne", answerJson.get("playerOne").getAsString())
                      .value("playerTwo", answerJson.get("playerTwo").getAsString())
                      .value("gameStatus", answerJson.get("gameStatus").getAsString())
                      .value("playerOneScores", answerJson.get("playerOneScores").getAsInt())
                      .value("playerTwoScores", answerJson.get("playerTwoScores").getAsInt())
                      .value("turn", answerJson.get("turn").getAsString()));

    } else {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("result", answerJson.get("result").getAsString()));
    }
    return CheckResult.correct();
  }

  private CheckResult testRevertApi(Integer gameId, Integer body, int status, String token, String answer,
                                    TestHint hint) {

    System.out.println(hint.toString());

    JsonObject jsonBody = new JsonObject();
    jsonBody.addProperty("gameId", gameId);
    jsonBody.addProperty("move", body);

    HttpResponse response = checkResponseStatus(token, jsonBody.toString(), status, "/api/game/revert", "PUT");

    JsonObject answerJson = getJson(answer).getAsJsonObject();

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("gameId", isInteger())
                      .value("playerOne", answerJson.get("playerOne").getAsString())
                      .value("playerTwo", answerJson.get("playerTwo").getAsString())
                      .value("gameStatus", answerJson.get("gameStatus").getAsString())
                      .value("playerOneScores", answerJson.get("playerOneScores").getAsInt())
                      .value("playerTwoScores", answerJson.get("playerTwoScores").getAsInt())
                      .value("turn", answerJson.get("turn").getAsString()));

    } else {
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("result", answerJson.get("result").getAsString()));
    }
    return CheckResult.correct();
  }

  private CheckResult testHistoryApi(String api, int status, String token, String answer,
                                     TestHint hint) {

    System.out.println(hint.toString());
    HttpResponse response = checkResponseStatus(token, "", status, api, "GET");

    // Check JSON in response
    if (response.getStatusCode() == 200) {
      JsonArray responseJson;
      try {
        responseJson = getJson(response.getContent()).getAsJsonArray();
      } catch (Exception e) {
        throw new WrongAnswer("Must be array of JSON's in answer");
      }
      JsonArray correctJson = getJson(answer).getAsJsonArray();
      if (responseJson.size() != correctJson.size()) {
        throw new WrongAnswer("Wrong size of array in response");
      }
      for (int i = 0; i < responseJson.size(); i++) {
        expect(responseJson.get(i).getAsJsonObject().toString()).asJson()
                .check(isObject()
                        .value("gameId", isInteger())
                        .value("move", isInteger())
                        .value("playerOne", correctJson.get(i).getAsJsonObject().get("playerOne").getAsString())
                        .value("playerTwo", correctJson.get(i).getAsJsonObject().get("playerTwo").getAsString())
                        .value("gameStatus", correctJson.get(i).getAsJsonObject().get("gameStatus").getAsString())
                        .value("playerOneScores", correctJson.get(i).getAsJsonObject().get("playerOneScores").getAsInt())
                        .value("playerTwoScores", correctJson.get(i).getAsJsonObject().get("playerTwoScores").getAsInt())
                        .value("turn", correctJson.get(i).getAsJsonObject().get("turn").getAsString()));
      }
    }  else {
      JsonObject answerJson = getJson(answer).getAsJsonObject();
      expect(response.getContent()).asJson().check(
              isObject()
                      .value("result", answerJson.get("result").getAsString()));
    }
    return CheckResult.correct();
  }


  @DynamicTest
  DynamicTesting[] dt = new DynamicTesting[]{
          // Negative tests
          () -> testTokenApi(ivanHoe, clientId, "clientSecret", HttpStatus.UNAUTHORIZED.value(),
                  new TestHint(tokenApi, "",
                          "Testing token endpoint with wrong client credentials")), // 1
          () -> testTokenApi(ivanHoe, "clientId", "clientSecret", HttpStatus.UNAUTHORIZED.value(),
                  new TestHint(tokenApi, "",
                          "Testing token endpoint with wrong client credentials")), // 2
          () -> testTokenApi(wrongUser, clientId, clientSecret, HttpStatus.BAD_REQUEST.value(),
                  new TestHint(tokenApi, "",
                          "Testing token endpoint with correct client credentials, but wrong user")), // 3
          //
          () -> getToken(ivanHoe, "update", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'update'")), // 4
          () -> checkToken(ivanHoe, new String[] {"update"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'update'")), // 5
          () -> testApi(apiCreate, "", "POST", 403, bearerToken, "ivanhoe@acme.com",
                  new TestHint(apiCreate, "", "The token with the wrong scope (update)" +
                          " should not be able to access api")), // 6
          //
          () -> getToken(ivanHoe, "write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'write'")), // 7
          () -> checkToken(ivanHoe, new String[] {"write"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'write'")), // 8
          () -> testApi(apiList, "", "GET", 403, bearerToken, "ivanhoe@acme.com",
                  new TestHint(apiList, "", "The token with the wrong scope (write)" +
                          " should not be able to access api with method GET")), // 9
          //
          () -> getToken(ivanHoe, "read", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read'")), // 10
          () -> checkToken(ivanHoe, new String[] {"read"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read'")), // 11
          () -> testApi(apiCreate, "", "POST", 403, bearerToken, "ivanhoe@acme.com",
                  new TestHint(apiCreate, "", "The token with the wrong scope (read)" +
                          " should not be able to access api with method POST")), // 12
          //
          () -> getToken(referee, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 13
          () -> checkToken(referee, new String[] {"read", "write"}, "ROLE_REFEREE", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read write'")), // 14
          () -> testApi(cancelApi, "", "PUT", 403, bearerToken, "judgedredd@acme.com",
                  new TestHint(cancelApi, "", "The token with the wrong scope (read write)" +
                          " should not be able to access api with method PUT")), // 15
          //
          () -> getToken(ivanHoe, "update", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'update'")), // 16
          () -> checkToken(ivanHoe, new String[] {"update"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'update'")), // 17
          () -> testApi(cancelApi, "", "PUT", 403, bearerToken, "ivanhoe@acme.com",
                  new TestHint(cancelApi, "", "The token with the wrong role (ROLE_GAMER)" +
                          " should not be able to access api with method PUT")), // 18

          // Tests for status API
          () -> getToken(ivanHoe, "read", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read'")), // 19
          () -> testStatusApi(404, bearerToken, emptyAnswer,
                  new TestHint(apiStatus, "",
                          "If the user does not participate in the game, then the endpoint must respond" +
                                  " with HTTP NOT FOUND status 404")), // 20

          // Tests for join API
          () -> testJoinApi(404, bearerToken, gameNotFound, 1001,
                  new TestHint(apiJoin + "/" + 1001, "",
                          "If a game with a specified id is not found, the endpoint must respond" +
                                  " with HTTP NOT FOUND status 404")), // 21

          // Tests for list API
          () -> testListApi(404, bearerToken, emptyArray), // 22


          // Tests for create API
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 16
          //
          () -> checkToken(ivanHoe, new String[] {"read", "write"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read write'")), // 17
          //
          () -> testCreateApi(wrongScore, 400, bearerToken, wrongScoreAnswer,
                  new TestHint(apiCreate, wrongScore,
                          "If the user specify wrong targetScore, endpoint" +
                                  " must respond with HTTP BAD REQUEST status 400")), // 18
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 19
          //
          () -> checkToken(ivanHoe, new String[] {"read", "write"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read write'")), // 20
          //
          () -> testCreateApi(correctScore, 200, bearerToken, answerRH,
                  new TestHint(apiCreate, correctScore,
                          "All conditions are met and the game must be created")), // 21
          //
          () -> getToken(wilhelmTell, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 22
          //
          () -> checkToken(wilhelmTell, new String[] {"read", "write"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read write'")), // 23
          //
          () -> testCreateApi(correctScore101, 200, bearerToken, answerWT,
                  new TestHint(apiCreate, correctScore101,
                          "All conditions are met and the game must be created")), // 24
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 25
          //
          () -> checkToken(ivanHoe, new String[] {"read", "write"}, "ROLE_GAMER", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read write'")), // 26
          //
          () -> testCreateApi(correctScore, 400, bearerToken, gameExistAnswer,
                  new TestHint(apiCreate, correctScore,
                          "If the user tries to create a game without completing another one," +
                                  " endpoint must respond with  HTTP BAD REQUEST status 400")), // 27
          //

          // Tests for status API
          () -> testStatusApi(200, bearerToken, statusAnswer,
                  new TestHint(apiStatus, "",
                          "Endpoint must return information about a current game for player.")), // 28

          // Tests for list API
          () -> testListApi(200, bearerToken, listAnswer), // 29

          // Tests for join API
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 30
          //
          () -> testJoinApi(400, bearerToken, autoGame, gameIds.get(0),
                  new TestHint(apiJoin + "/" + gameIds.get(0), "",
                          "If a player tries to join his own game," +
                                  " the endpoint must respond with HTTP BAD REQUEST status 400")), // 31
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 32
          () -> testJoinApi(200, bearerToken, joinAnswerIH, gameIds.get(0),
                  new TestHint(apiJoin + "/" + gameIds.get(0), "",
                          "Endpoint must respond with HTTP OK status 200 and JSON about game" +
                                  " if user can join a game with specified id" +
                                  " (game status is created and the user does not participate in other games):")), // 33
          //
          () -> testJoinApi(400, bearerToken, gameInProgress, gameIds.get(0),
                  new TestHint(apiJoin + "/" + gameIds.get(0), "",
                          "If a player tries to join a game which status is not open for joining " +
                                  "(status is not created), the endpoint must respond " +
                                  "with HTTP BAD REQUEST status 400")), // 34
          //
          () -> testJoinApi(400, bearerToken, unFinishedGame, gameIds.get(1),
                  new TestHint(apiJoin + "/" + gameIds.get(1), "",
                          "If a user is involved in other game on a server," +
                                  " the endpoint must respond with HTTP BAD REQUEST status 400 ")), // 35
          //

          // Tests for list API
          () -> testListApi(200, bearerToken, listAnswer2), // 36

          // Tests for throws API
          () -> testThrowsApi(correctThrows1, 400, bearerToken, wrongTurn,
                  new TestHint(apiThrows, correctThrows1,
                          "If a player tries to make a move out of turn," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 37
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 38
          //
          () -> testThrowsApi(correctThrows1, 200, bearerToken, throwsAnswer1,
                  new TestHint(apiThrows, correctThrows1,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 39
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 40
          //
          () -> testThrowsApi(correctThrows2, 200, bearerToken, throwsAnswer2,
                  new TestHint(apiThrows, correctThrows2,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 41
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 42
          //
          () -> testThrowsApi(correctThrows3, 200, bearerToken, throwsAnswer3,
                  new TestHint(apiThrows, correctThrows3,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 43
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 44
          //
          () -> testThrowsApi(correctThrows2, 200, bearerToken, throwsAnswer4,
                  new TestHint(apiThrows, correctThrows2,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 45
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 46
          //
          () -> testThrowsApi(correctThrows3, 200, bearerToken, throwsAnswer5,
                  new TestHint(apiThrows, correctThrows3,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 47
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 48
          //
          () -> testThrowsApi(correctThrows2, 200, bearerToken, throwsAnswer6,
                  new TestHint(apiThrows, correctThrows2,
                          "If user BUST turn must changed, but scores not" +
                                  " endpoint must respond with HTTP OK status 200")), // 49
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 50
          //
          () -> testThrowsApi(correctThrows4, 200, bearerToken, throwsAnswer7,
                  new TestHint(apiThrows, correctThrows4,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 51
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 52
          //
          () -> testThrowsApi(correctThrows5, 200, bearerToken, throwsAnswer8,
                  new TestHint(apiThrows, correctThrows5,
                          "If user BUST turn must changed, but scores not" +
                                  " endpoint must respond with HTTP OK status 200")), // 53
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 54
          //
          () -> testThrowsApi(correctThrows6, 200, bearerToken, throwsAnswer9,
                  new TestHint(apiThrows, correctThrows6,
                          "If user wins game status and user score must changes, but turn not" +
                                  " endpoint must respond with HTTP OK status 200")), // 55
          //
          () -> testThrowsApi(correctThrows6, 404, bearerToken, noGames,
                  new TestHint(apiThrows, correctThrows6,
                          "If no game is found for the user" +
                                  " endpoint must respond with HTTP NOT FOUND status 404")), // 56
          //

          // Tests for status API
          () -> testStatusApi(200, bearerToken, statusAnswer2,
                  new TestHint(apiStatus, "",
                          "Endpoint must return information about the last game played.")), // 57

          // Tests for join API
          () -> getToken(wilhelmTell, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 58
          //
          () -> testJoinApi(400, bearerToken, gameInProgress, gameIds.get(0),
                  new TestHint(apiJoin + "/" + gameIds.get(0), "",
                          "If a player tries to join a game which status is not open for joining " +
                                  "(status is not created), the endpoint must respond " +
                                  "with HTTP BAD REQUEST status 400")), // 59
          //

          // Tests for list API
          () -> testListApi(200, bearerToken, listAnswer3), // 60
          //

          // Tests for join API
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 61
          () -> testJoinApi(200, bearerToken, joinAnswerRH, gameIds.get(1),
                  new TestHint(apiJoin + "/" + gameIds.get(0), "",
                          "Endpoint must respond with HTTP OK status 200 and JSON about game" +
                                  " if user can join a game with specified id" +
                                  " (game status is created and the user does not participate in other games):")), // 62
          //

          // Tests for list API
          () -> testListApi(200, bearerToken, listAnswer4), // 63
          //

          // Tests for throws API
          () -> getToken(wilhelmTell, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 64
          //
          () -> testThrowsApi(wrongThrows1, 400, bearerToken, wrongThrowsAnswer,
                  new TestHint(apiThrows, wrongThrows1,
                          "If a player enter a wrong throws," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 65
          //
          () -> testStatusApi(200, bearerToken, statusAnswer3,
                  new TestHint(apiStatus, "",
                          "Game state after sending wrong information about throws, must not be changed")), // 66
          //
          () -> testThrowsApi(wrongThrows2, 400, bearerToken, wrongThrowsAnswer,
                  new TestHint(apiThrows, wrongThrows2,
                          "If a player enter a wrong throws," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 67
          //
          () -> testThrowsApi(wrongThrows3, 400, bearerToken, wrongThrowsAnswer,
                  new TestHint(apiThrows, wrongThrows3,
                          "If a player enter a wrong throws," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 68
          //
          () -> testThrowsApi(wrongThrows4, 400, bearerToken, wrongThrowsAnswer,
                  new TestHint(apiThrows, wrongThrows4,
                          "If a player enter a wrong throws," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 69
          //
          () -> testThrowsApi(wrongThrows5, 400, bearerToken, wrongThrowsAnswer,
                  new TestHint(apiThrows, wrongThrows5,
                          "If a player enter a wrong throws," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 70
          //
          () -> testThrowsApi(wrongBustThrows1, 400, bearerToken, wrongThrowsAnswer,
                  new TestHint(apiThrows, wrongBustThrows1,
                          "If a player enter a wrong throws (extra throws)," +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 71
          //
          () -> testStatusApi(200, bearerToken, statusAnswer3,
                  new TestHint(apiStatus, "",
                          "Game state after sending wrong information about throws, must not be changed")), // 72
          //
          () -> testThrowsApi(CorrectBustThrows1, 200, bearerToken, statusAnswer4,
                  new TestHint(apiThrows, CorrectBustThrows1,
                          "If user BUST turn must changed, but scores not" +
                                  " endpoint must respond with HTTP OK status 200")), // 73
          //
          () -> testStatusApi(200, bearerToken, statusAnswer4,
                  new TestHint(apiStatus, "",
                          "If user BUST turn must changed, but scores not" +
                                  " endpoint must respond with HTTP OK status 200")), // 74
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 75
          //
          () -> testThrowsApi(winThrowsRH, 200, bearerToken, statusAnswer5,
                  new TestHint(apiThrows, winThrowsRH,
                          "If user wins game status and user score must changes, but turn not" +
                                  " endpoint must respond with HTTP OK status 200")), // 76

          // Tests for status API
          () -> testStatusApi(200, bearerToken, statusAnswer6,
                  new TestHint(apiStatus, "",
                          "Endpoint must return information about the last game played.")), // 77


          // Tests for history API
          () -> testHistoryApi(historyApi + gameIds.get(0), 200, bearerToken,
                  historyAnswer1, new TestHint(historyApi, "",
                          "If game with specified gameid is found and status of game is not a \"created\"" +
                                  " endpoint must respond with HTTP OK status 200")), // 78
          //
          () -> testHistoryApi(historyApi + "unknown", 400, bearerToken,
                  historyBad, new TestHint(historyApi + "unknown", "",
                          "If gameId is not a number" +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 78
          //
          () -> testHistoryApi(historyApi + -1, 400, bearerToken,
                  historyBad, new TestHint(historyApi + -1, "",
                          "If game with gameId < 0" +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 78
          //

          // Tests for cancelling
          () -> getToken(referee, "read update", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Request token for referee")), // 79
          //
          () -> checkToken(referee, new String[] {"read", "update"}, "ROLE_REFEREE", new TestHint(tokenApi, "",
                  "Checking token 'scope' value, it must be - 'read update'")), // 80
          //
          () -> testCancelApi(gameIds.get(0), "Nobody wins!", 400, bearerToken,
                  cancelAnswer1, new TestHint(cancelApi, "",
                          "If game with specified gameid is found and status of game is \"somebody wins!\"" +
                                  " endpoint must respond with HTTP BAD REQUEST status 400")), // 81

          () -> getToken(wilhelmTell, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 82
          //
          () -> testCreateApi(correctScore101, 200, bearerToken, answerWT,
                  new TestHint(apiCreate, correctScore101,
                          "All conditions are met and the game must be created")), // 83
          //
          () -> getToken(referee, "read update", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Request token for referee")), // 84
          //
          () -> testCancelApi(gameIds.get(gameIds.size() - 1), "Somebody wins!", 400, bearerToken,
                  cancelAnswer4, new TestHint(cancelApi, "",
                          "If the player specified in the status field is not a participant of the game" +
                                  " endpoint must respond with HTTP BAD REQUEST status 400 and change game status")), // 85
          //
          () -> testCancelApi(gameIds.get(gameIds.size() - 1), "Nobody wins!", 200, bearerToken,
                  cancelAnswer2, new TestHint(cancelApi, "",
                          "If game with specified gameid is found and status of game is not \"somebody wins!\"" +
                                  " endpoint must respond with HTTP OK status 200 and change game status")), // 85
          //

          // Test reverting games
          () -> testRevertApi(gameIds.get(0), 22, 400, bearerToken,
                  revertNotFound, new TestHint(revertApi, "",
                          "If game with specified gameId is found and move is not exist" +
                                  " endpoint must respond with HTTP BAD REQUEST status 400 and game state not must be changed")), // 87
          () -> testRevertApi(gameIds.get(0), 9, 400, bearerToken,
                  revertLast, new TestHint(revertApi, "",
                          "If game with specified gameId is found and move is not exist" +
                                  " endpoint must respond with HTTP OK status 400 and game state must not be changed")), // 87
          () -> testHistoryApi(historyApi + gameIds.get(0), 200, bearerToken,
                  historyAnswer1, new TestHint(historyApi, "",
                          "Game state must not be changed after wrong requests")), // 78
          () -> testRevertApi(gameIds.get(0), 2, 400, bearerToken,
                  revertOver, new TestHint(revertApi, "",
                          "If game with specified gameid is found and game is over" +
                                  " endpoint must respond with HTTP BAD REQUEST status 400 and game state must not be changed")), // 87
          //
          () -> testHistoryApi(historyApi + gameIds.get(0), 200, bearerToken,
                  historyAnswer1, new TestHint(historyApi, "",
                          "Game history after revert must not be changed" +
                                  " endpoint must respond with HTTP OK status 200")), // 88
          //

          // Let's play and revert
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 16
          //
          () -> testCreateApi(correctScore, 200, bearerToken, answerRH,
                  new TestHint(apiCreate, correctScore,
                          "All conditions are met and the game must be created")), // 21
          //
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 61
          //
          () -> testJoinApi(200, bearerToken, joinAnswerRH1, gameIds.get(gameIds.size() - 1),
                  new TestHint(apiJoin + "/" + gameIds.get(gameIds.size() - 1), "",
                          "Endpoint must respond with HTTP OK status 200 and JSON about game" +
                                  " if user can join a game with specified id" +
                                  " (game status is created and the user does not participate in other games):")), //
          //
          () -> getToken(ivanHoe, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 16
          //
          () -> testThrowsApi(correctThrows1, 200, bearerToken, statusAnswer7,
                  new TestHint(apiThrows, correctThrows1,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 73
          () -> getToken(robinHood, "read write", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Testing token endpoint with correct credentials and correct user and scope 'read write'")), // 75
          () -> testThrowsApi(correctThrows2, 200, bearerToken, statusAnswer8,
                  new TestHint(apiThrows, correctThrows2,
                          "If user makes correct throws scores and turn must changed," +
                                  " endpoint must respond with HTTP OK status 200")), // 73
          () -> testHistoryApi(historyApi + gameIds.get(gameIds.size() - 1), 200, bearerToken,
                  historyAnswer3, new TestHint(historyApi, "",
                          "Game history must changes after throws," +
                                  " endpoint must respond with HTTP OK status 200")), // 88
          () -> getToken(referee, "read update", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Request token for referee")), // 84
          () -> testRevertApi(gameIds.get(gameIds.size() - 1), 1, 200, bearerToken,
                  revertAnswer1, new TestHint(revertApi, "",
                          "If game with specified gameId is found and move is exist" +
                                  " endpoint must respond with HTTP OK status 200 and game state must be changed")), // 87
          () -> testHistoryApi(historyApi + gameIds.get(gameIds.size() - 1), 200, bearerToken,
                  historyAnswer4, new TestHint(historyApi, "",
                          "Game history must changes after revert to move - 1," +
                                  " endpoint must respond with HTTP OK status 200")), // 88

          // Tests for cancel API, not found
          () -> getToken(referee, "read update", HttpStatus.OK.value(), new TestHint(tokenApi, "",
                  "Request token for referee")), // 23

          () -> testCancelApi(1001, "Nobody wins!", 404, bearerToken,
                  cancelAnswer3, new TestHint(cancelApi, "",
                          "If game with specified gameid is not found" +
                                  " endpoint must respond with HTTP NOT FOUND status 404")), // 24

          // Tests for history API, not found
          () -> testHistoryApi(historyApi + 1001, 404, bearerToken,
                  historyNotFound, new TestHint(historyApi, "",
                          "If game with specified gameId is not found" +
                                  " endpoint must respond with HTTP NOT FOUND status 404")), // 25

          // Tests for revert API, not found
          () -> testRevertApi(1001, 2, 404, bearerToken,
                  historyNotFound, new TestHint(revertApi, "",
                          "If game with specified gameId is not found" +
                                  " endpoint must respond with HTTP OK status 404")), // 26





  };
}