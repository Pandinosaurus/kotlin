// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: proto_tcs.proto
// Protobuf Java Version: 4.28.2

package org.jetbrains.kotlin.gradle.idea.proto.generated.tcs;

public interface IdeaKotlinResolvedBinaryDependencyProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
   * @return Whether the extras field is set.
   */
  boolean hasExtras();
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
   * @return The extras.
   */
  org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto getExtras();
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
   */
  org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProtoOrBuilder getExtrasOrBuilder();

  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto coordinates = 2;</code>
   * @return Whether the coordinates field is set.
   */
  boolean hasCoordinates();
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto coordinates = 2;</code>
   * @return The coordinates.
   */
  org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto getCoordinates();
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto coordinates = 2;</code>
   */
  org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProtoOrBuilder getCoordinatesOrBuilder();

  /**
   * <code>optional string binary_type = 3;</code>
   * @return Whether the binaryType field is set.
   */
  boolean hasBinaryType();
  /**
   * <code>optional string binary_type = 3;</code>
   * @return The binaryType.
   */
  java.lang.String getBinaryType();
  /**
   * <code>optional string binary_type = 3;</code>
   * @return The bytes for binaryType.
   */
  com.google.protobuf.ByteString
      getBinaryTypeBytes();

  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto classpath = 4;</code>
   * @return Whether the classpath field is set.
   */
  boolean hasClasspath();
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto classpath = 4;</code>
   * @return The classpath.
   */
  org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto getClasspath();
  /**
   * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto classpath = 4;</code>
   */
  org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProtoOrBuilder getClasspathOrBuilder();
}
