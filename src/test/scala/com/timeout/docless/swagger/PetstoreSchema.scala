package com.timeout.docless.swagger

import com.timeout.docless.schema.JsonSchema
import com.timeout.docless.swagger.Info.License
import com.timeout.docless.swagger.Responses.{Header, Response}

object PetstoreSchema {
  import JsonSchema._
  case class Pet(id: Int, name: String, tag: Option[String])
  case class Error(code: Int, message: Option[String])

  object Schemas {
    val pet = JsonSchema.deriveFor[Pet]
    val error = JsonSchema.deriveFor[Error]
    val all = List(pet, error).map(_.definition)
  }

  def apply(): APISchema = {
    val errorResponse = Response(
      description = "API error",
      schema = Some(TypeRef(Schemas.error))
    )

    val petIdParam = Parameter.path(
      name = "id",
      description = Some("The pet id"),
      format = Some(Format.Int32)
    ).asInteger

    val limitParam = Parameter.query(
      name = "limit",
      description = Some("How many items to return at one time (max 100)"),
      format = Some(Format.Int32)
    ).asInteger

    val petResp = Response(
      description = "pet response",
      schema = Some(ArrayRef(Schemas.pet))
    )

    APISchema(
      info = Info(title = "Swagger petstore", license = Some(License(name = "MIT"))),
      host = "petstore.swagger.io",
      basePath = "/v1",
      schemes = Set(Scheme.Http),
      consumes = Set("application/json"),
      produces = Set("application/json")
    ).defining(Schemas.all: _*)
      .withPaths(
        "/pets"
          .Get(Operation(
            operationId = Some("listPets"),
            tags = List("pets"),
            summary = Some("List all pets"))
            .withParams(limitParam)
            .responding(errorResponse)(
              200 -> Response(
                schema = Some(ArrayRef(Schemas.pet)),
                description = "A paged array of pets"
              ).withHeaders(
                "x-next" -> Header(
                  `type` = Type.String,
                  description = Some("A link to the next page of responses")
                )
              )
            ))
          .Post(Operation(
            operationId = Some("createPets"),
            tags = List("pets"),
            summary = Some("Create a pet"))
            .responding(errorResponse)(201 -> petResp))
        ,
        "/pets/{id}"
          .Get(Operation(
            parameters = List(petIdParam),
            operationId = Some("showPetById"),
            tags = List("pets"),
            summary = Some("info for a specific pet"))
            .responding(errorResponse)(200 -> petResp))
          .Delete(Operation(
            operationId = Some("deletePetById"),
            tags = List("pets"),
            summary = Some("deletes a single pet"))
            .responding(errorResponse)(204 -> petResp))
      )
  }
}
