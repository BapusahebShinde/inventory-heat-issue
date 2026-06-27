cmake_minimum_required(VERSION 3.22.1)

project("native-lib")

include_directories(${CMAKE_SOURCE_DIR}/include)

add_library(
        native-lib
        SHARED
        native-lib.c
        main.c
        itek_nstd.c
        itek_sgtin96.c
        itek_tatagid.c
)

find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ${log-lib}
)
