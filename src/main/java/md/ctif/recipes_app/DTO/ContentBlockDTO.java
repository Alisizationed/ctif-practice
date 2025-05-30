//package md.ctif.recipes_app.DTO;
//import com.fasterxml.jackson.annotation.JsonSubTypes;
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//
//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.NAME,
//        include = JsonTypeInfo.As.PROPERTY,
//        property = "type"
//)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = ParagraphBlockDTO.class, name = "paragraph"),
//        @JsonSubTypes.Type(value = ImageBlockDTO.class, name = "image")
//})
//public sealed interface ContentBlockDTO
//        permits ParagraphBlockDTO, ImageBlockDTO {}
//
