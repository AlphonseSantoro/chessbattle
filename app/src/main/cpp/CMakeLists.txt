# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library(# Specifies the name of the library.
        jstockfish

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        jstockfish.cpp
        jstockfish_Uci.h
        jstockfish_Position.h
        syzygy/tbprobe.cpp
        syzygy/tbprobe.h
        evaluate_trace.cpp
        evaluate_trace.h
        positionstate.cpp
        positionstate.h
        benchmark.cpp
        bitbase.cpp
        bitboard.cpp
        bitboard.h
        endgame.cpp
        endgame.h
        evaluate.cpp
        evaluate.h
        main.cpp
        Makefile
        material.cpp
        material.h
        misc.cpp
        misc.h
        movegen.cpp
        movegen.h
        movepick.cpp
        movepick.h
        pawns.cpp
        pawns.h
        position.cpp
        position.h
        psqt.cpp
        search.cpp
        search.h
        thread.cpp
        thread.h
        thread_win32.h
        timeman.cpp
        timeman.h
        tt.cpp
        tt.h
        types.h
        uci.cpp
        uci.h
        ucioption.cpp)


find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        log-lib

        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log )

# Links your native library against one or more other native libraries.
target_link_libraries( # Specifies the target library.
        jstockfish

        # Links the log library to the target library.
        ${log-lib} )