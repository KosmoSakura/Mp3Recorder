cmake_minimum_required(VERSION 3.10.2)

project("jni")

add_subdirectory(libmp3lame)

#file(GLOB source_file ./*.cpp)
file(GLOB source_file ${CMAKE_CURRENT_SOURCE_DIR}/*.cpp)
add_library(jni SHARED ${source_file})

#include_directories(libmp3lame)

find_library(log-lib log)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/armeabi-v7a")

target_link_libraries(jni lamemp3 ${log-lib})
