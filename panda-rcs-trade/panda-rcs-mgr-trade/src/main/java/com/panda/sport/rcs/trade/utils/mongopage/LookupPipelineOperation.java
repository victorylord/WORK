package com.panda.sport.rcs.trade.utils.mongopage;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.Document;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Variable;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;

public class LookupPipelineOperation implements AggregationOperation {

	private final String from;
	private final List<Variable<String>> let;
	private final List<Bson> pipeline;
	private final String as;
	
	CodecRegistry codecRegistry = MongoClientSettings.getDefaultCodecRegistry();

	/**
	 * Creates a new {@link LookupOperation} for the given {@link Field}s.
	 *
	 * @param from must not be {@literal null}.
	 * @param localField must not be {@literal null}.
	 * @param foreignField must not be {@literal null}.
	 * @param as must not be {@literal null}.
	 */
	public LookupPipelineOperation(final String from, @Nullable final List<Variable<String>> let, final List<Bson> pipeline,
            final String as) {

		Assert.notNull(from, "From must not be null!");
		Assert.notNull(as, "As must not be null!");

		this.from = from;
		this.let = let;
		this.pipeline = pipeline;
		this.as = as;
	}

	@Override
	public Document toDocument(AggregationOperationContext context) {
		BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());

        writer.writeStartDocument();

        writer.writeStartDocument("$lookup");

        writer.writeString("from", from);

        if (let != null) {
            writer.writeStartDocument("let");

            for (Variable<?> variable : let) {
                writer.writeName(variable.getName());
                encodeValue(writer, variable.getValue(), codecRegistry);
            }

            writer.writeEndDocument();
        }

        writer.writeName("pipeline");
        writer.writeStartArray();
        for (Bson stage : pipeline) {
            encodeValue(writer, stage, codecRegistry);
        }
        writer.writeEndArray();
        writer.writeString("as", as);
        writer.writeEndDocument();
        
        return Document.parse(writer.getDocument().toJson());
	}
	
	public static void main(String[] args) {
		List<Variable<String>> lets = new ArrayList<>();
        Variable<String> let1 = new Variable<>("name", "$name");
        lets.add(let1);
        
        Bson pipeline = new BasicDBObject("$match",
                new BasicDBObject("$expr",
                        new BasicDBObject("$or", new BasicDBObject[]{
                                new BasicDBObject("$eq", new String[]{"$name", "$$name"}),
                                new BasicDBObject("$eq", new String[]{"$code", "$$code"})
                        })
                ));
        List<Bson> pipelines = new ArrayList<>();
        pipelines.add(pipeline);

        new LookupPipelineOperation("aa", lets, pipelines, "bb").toDocument(null);
	}

	
    @SuppressWarnings("unchecked")
    <TItem> void encodeValue(final BsonDocumentWriter writer, final TItem value, final CodecRegistry codecRegistry) {
        if (value == null) {
            writer.writeNull();
        } else if (value instanceof Bson) {
            ((Encoder) codecRegistry.get(BsonDocument.class)).encode(writer,
                                                                     ((Bson) value).toBsonDocument(BsonDocument.class, codecRegistry),
                                                                     EncoderContext.builder().build());
        } else {
            ((Encoder) codecRegistry.get(value.getClass())).encode(writer, value, EncoderContext.builder().build());
        }
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#getOperator()
	 */
//	@Override
//	public String getOperator() {
//		return "$lookup";
//	}

}
