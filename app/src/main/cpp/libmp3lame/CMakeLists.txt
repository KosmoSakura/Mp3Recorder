cmake_minimum_required(VERSION 3.10.2)

project("lamemp3")

file(GLOB source_file ${CMAKE_CURRENT_SOURCE_DIR}/*.c)
add_library(lamemp3 SHARED ${source_file})

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DSTDC_HEADERS")