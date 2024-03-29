// Basically, this file looks like uci.cpp file of Stockfish.

#include <iostream>
#include <sstream>
#include <string>

#include "evaluate.h"
#include "evaluate_trace.h"
#include "movegen.h"
#include "position.h"
#include "positionstate.h"
#include "search.h"
#include "thread.h"
#include "timeman.h"
#include "tt.h"
#include "uci.h"
#include "syzygy/tbprobe.h"

#include "jstockfish_Uci.h"
#include "jstockfish_Position.h"

using namespace std;

//------------------------------------------------------------------------------

#ifdef __cplusplus
extern "C" {
#endif

jint JNI_OnLoad(JavaVM* vm, void* reserved);

#ifdef __cplusplus
}
#endif

//------------------------------------------------------------------------------

static JavaVM* jvm = NULL;
jobject uciObject = NULL;

// Call jstockfish.Uci.onOutput
void uci_out(string output) {
  // Should another thread need to access the Java VM, it must first call
  // AttachCurrentThread() to attach itself to the VM and obtain a JNI interface pointer
  JNIEnv *env;

  if (jvm->AttachCurrentThread(&env, NULL) != 0) {
    cout << "[JNI] Could not AttachCurrentThread" << endl;
    return;
  }

  jclass uciClass = env->GetObjectClass(uciObject);
  jmethodID onOutput = env->GetMethodID(uciClass, "onOutput", "(Ljava/lang/String;)V");
  jstring js = env->NewStringUTF(output.c_str());

  env->CallVoidMethod(uciObject, onOutput, js);
  jvm->DetachCurrentThread();
}

//------------------------------------------------------------------------------

// FEN string of the initial position, normal chess
static const char* START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

// A list to keep track of the position states along the setup moves (from the
// start position to the position just before the search starts). Needed by
// 'draw by repetition' detection.
static StateListPtr gStates(new std::deque<StateInfo>(1));

// The root position
static Position gPos;

//------------------------------------------------------------------------------

bool initJvm(JavaVM *vm) {
  jvm = vm;

  JNIEnv *env;
  int stat = jvm->GetEnv((void **) &env, JNI_VERSION_1_6);
	if (stat != JNI_OK) {
    cout << "[JNI] Could not GetEnv" << endl;
    return false;
  }



  return true;
}

bool read_position(
  JNIEnv *env, jboolean chess960, jstring position,
  StateListPtr& states, Position& pos
) {
  const char *chars = env->GetStringUTFChars(position, NULL);
  istringstream is(chars);
  env->ReleaseStringUTFChars(position, chars);

  Move m;
  string token, fen;

  is >> token;

  if (token == "startpos") {
    fen = START_FEN;
    is >> token;  // Consume "moves" token if any
  } else if (token == "fen") {
    while (is >> token && token != "moves") {
      fen += token + " ";
    }
  } else {
    return false;
  }
  pos.set(fen, chess960, &states->back(), Threads.main());

  // Parse move list (if any)
  while (is >> token) {
    m = UCI::to_move(pos, token);
    if (m == MOVE_NONE) {
      return false;
    }

    states->push_back(StateInfo());
    pos.do_move(m, states->back());
  }

  return true;
}

// https://hxim.github.io/Stockfish-Evaluation-Guide/
double whiteScore(int term) {
  double mg = Trace::scores[term][WHITE][MG];
  double eg = Trace::scores[term][WHITE][EG];

  // Ignore MG if it's too small, same for EG.
  // Otherwise return the average of the two.
  if (-0.01 < mg && mg < 0.01) return eg;
  if (-0.01 < eg && eg < 0.01) return mg;
  return (mg + eg) / 2;
}

jdoubleArray whiteScores(JNIEnv *env, Position& pos) {
  Eval::set_scores(pos);

  double arr[12];

  arr[0] = Trace::scores[Trace::TOTAL][WHITE][ALL_PIECES];

  arr[1] = whiteScore(PAWN);
  arr[2] = whiteScore(KNIGHT);
  arr[3] = whiteScore(BISHOP);
  arr[4] = whiteScore(ROOK);
  arr[5] = whiteScore(QUEEN);
  arr[6] = whiteScore(KING);

  arr[7] = whiteScore(Trace::IMBALANCE);
  arr[8] = whiteScore(Trace::MOBILITY);
  arr[9] = whiteScore(Trace::THREAT);
  arr[10] = whiteScore(Trace::PASSED);
  arr[11] = whiteScore(Trace::SPACE);

  int length = sizeof(arr) / sizeof(double);
  jdoubleArray ret = env->NewDoubleArray(length);
  env->SetDoubleArrayRegion(ret, 0, length, arr);

  return ret;
}

bool isLegal(Position& pos, JNIEnv *env, jstring move) {
  const char *chars = env->GetStringUTFChars(move, NULL);
  string str = chars;
  env->ReleaseStringUTFChars(move, chars);

  Move m = UCI::to_move(pos, str);
  return pos.pseudo_legal(m);
}

//------------------------------------------------------------------------------

namespace PSQT {
  void init();
}

jint JNI_OnLoad(JavaVM *vm, void*) {
	if (!initJvm(vm)) return JNI_VERSION_1_6;

  // See Stockfish's main function
  UCI::init(Options);
  PSQT::init();
  Bitboards::init();
  Position::init();
  Bitbases::init();
  Search::init();
  Pawns::init();
  Tablebases::init(Options["SyzygyPath"]);
  TT.resize(Options["Hash"]);
  Threads.set(Options["Threads"]);
  Search::clear();

  JNIEnv * env;
  vm->GetEnv((void **)&env, JNI_VERSION_1_6);
  jclass tmp = env->FindClass("no/kristiania/alphonsesantoro/chessbattle/util/OutputListenerImpl");
  jmethodID tmpM = env->GetMethodID(tmp, "<init>", "()V");
  jobject listener = env->NewObject(tmp, tmpM);
  uciObject = env->NewGlobalRef(listener);

  gPos.set(START_FEN, Options["UCI_Chess960"], &gStates->back(), Threads.main());

  return JNI_VERSION_1_6;
}

JNIEXPORT jstring JNICALL Java_jstockfish_Uci_uci(JNIEnv *env, jclass) {
  stringstream uci;
  uci << "id name " << engine_info(true)
      << "\n"       << Options
      << "\nuciok";

  jstring ret = env->NewStringUTF(uci.str().c_str());
  return ret;
}

JNIEXPORT jboolean JNICALL Java_jstockfish_Uci_setOption(JNIEnv *env, jclass, jstring name, jstring value) {
  const char *chars_name = env->GetStringUTFChars(name, NULL);
  string str_name = chars_name;
  env->ReleaseStringUTFChars(name, chars_name);

  const char *chars_value = env->GetStringUTFChars(value, NULL);
  string str_value = chars_value;
  env->ReleaseStringUTFChars(value, chars_value);

  if (Options.count(str_name)) {
      Options[str_name] = str_value;
      return true;
  } else {
      return false;
  }
}

JNIEXPORT void JNICALL Java_jstockfish_Uci_newGame(JNIEnv *, jclass) {
  Search::clear();
  Tablebases::init(Options["SyzygyPath"]);
  Time.availableNodes = 0;
}

JNIEXPORT jboolean JNICALL Java_jstockfish_Uci_position(JNIEnv *env, jclass, jstring position) {
  gStates = StateListPtr(new std::deque<StateInfo>(1));
  return read_position(
    env, Options["UCI_Chess960"], position,
    gStates, gPos
  );
}

//------------------------------------------------------------------------------

JNIEXPORT void JNICALL Java_jstockfish_Uci_go(JNIEnv *env, jclass, jstring options) {
  // Similar to Stockfish's UCI#go

  const char *chars = env->GetStringUTFChars(options, NULL);
  istringstream is(chars);
  env->ReleaseStringUTFChars(options, chars);

  Search::LimitsType limits;
  string token;
  bool ponderMode = false;

  limits.startTime = now();

  while (is >> token)
    if (token == "searchmoves")
      while (is >> token)
        limits.searchmoves.push_back(UCI::to_move(gPos, token));

    else if (token == "wtime")     is >> limits.time[WHITE];
    else if (token == "btime")     is >> limits.time[BLACK];
    else if (token == "winc")      is >> limits.inc[WHITE];
    else if (token == "binc")      is >> limits.inc[BLACK];
    else if (token == "movestogo") is >> limits.movestogo;
    else if (token == "depth")     is >> limits.depth;
    else if (token == "nodes")     is >> limits.nodes;
    else if (token == "movetime")  is >> limits.movetime;
    else if (token == "mate")      is >> limits.mate;
    else if (token == "infinite")  limits.infinite = 1;
    else if (token == "ponder")    ponderMode = true;

  Threads.start_thinking(gPos, gStates, limits, ponderMode);
}

JNIEXPORT void JNICALL Java_jstockfish_Uci_stop(JNIEnv *, jclass) {
  Threads.stop = true;
}

JNIEXPORT void JNICALL Java_jstockfish_Uci_ponderHit(JNIEnv *, jclass) {
  Threads.ponder = false;
}

//------------------------------------------------------------------------------

JNIEXPORT jstring JNICALL Java_jstockfish_Uci_d(JNIEnv *env, jclass) {
  stringstream ss;
  ss << gPos;
  jstring ret = env->NewStringUTF(ss.str().c_str());
  return ret;
}

JNIEXPORT void JNICALL Java_jstockfish_Uci_flip(JNIEnv *, jclass) {
  gPos.flip();
}

JNIEXPORT jstring JNICALL Java_jstockfish_Uci_eval(JNIEnv *env, jclass) {
  stringstream ss;
  ss << Eval::trace(gPos);
  jstring ret = env->NewStringUTF(ss.str().c_str());
  return ret;
}

JNIEXPORT jdoubleArray JNICALL Java_jstockfish_Uci_whiteScores(JNIEnv *env, jclass) {
  return whiteScores(env, gPos);
}

//------------------------------------------------------------------------------

JNIEXPORT jboolean JNICALL Java_jstockfish_Uci_isLegal(
  JNIEnv *env, jclass, jstring move
) {
  return isLegal(gPos, env, move);
}

JNIEXPORT jstring JNICALL Java_jstockfish_Uci_fen(JNIEnv *env, jclass) {
  string fen = gPos.fen();
  jstring ret = env->NewStringUTF(fen.c_str());
  return ret;
}

JNIEXPORT jint JNICALL Java_jstockfish_Uci_ordinalState(JNIEnv *, jclass) {
  return positionstate(gPos);
}

//------------------------------------------------------------------------------

JNIEXPORT jdoubleArray JNICALL Java_jstockfish_Position_whiteScores(
  JNIEnv *env, jclass, jboolean chess960, jstring position
) {
  StateListPtr states(new std::deque<StateInfo>(1));
  Position pos;
  read_position(env, chess960, position, states, pos);

  // When read_position above returns false, the result is somewhere in the middle
  return whiteScores(env, pos);
}

JNIEXPORT jboolean JNICALL Java_jstockfish_Position_isLegal(
  JNIEnv *env, jclass, jboolean chess960, jstring position, jstring move
) {
  StateListPtr states(new std::deque<StateInfo>(1));
  Position pos;
  return
    read_position(env, chess960, position, states, pos) &&
    isLegal(pos, env, move);
}

JNIEXPORT jstring JNICALL Java_jstockfish_Position_fen(
  JNIEnv *env, jclass, jboolean chess960, jstring position
) {
  StateListPtr states(new std::deque<StateInfo>(1));
  Position pos;
  read_position(env, chess960, position, states, pos);

  // When read_position above returns false, the result is somewhere in the middle
  string fen = pos.fen();
  jstring ret = env->NewStringUTF(fen.c_str());
  return ret;
}

JNIEXPORT jint JNICALL Java_jstockfish_Position_ordinalState(
  JNIEnv *env, jclass, jboolean chess960, jstring position
) {
  StateListPtr states(new std::deque<StateInfo>(1));
  Position pos;
  read_position(env, chess960, position, states, pos);

  // When read_position above returns false, the result is somewhere in the middle
  return positionstate(pos);
}
