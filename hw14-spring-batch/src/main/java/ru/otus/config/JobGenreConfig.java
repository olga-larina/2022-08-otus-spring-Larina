package ru.otus.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import ru.otus.domain.mongo.Genre;
import ru.otus.service.reader.MongoItemRestartableReader;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;

@Configuration
public class JobGenreConfig {

    private static final int CHUNK_SIZE = 5;

    private final Logger logger = LoggerFactory.getLogger("Batch-Genre");

    private final StepBuilderFactory stepBuilderFactory;
    private final MongoTemplate mongoTemplate;
    private final DataSource dataSource;

    public JobGenreConfig(StepBuilderFactory stepBuilderFactory,
                          MongoTemplate mongoTemplate,
                          DataSource dataSource) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.mongoTemplate = mongoTemplate;
        this.dataSource = dataSource;
    }

    @Bean
    public MongoItemRestartableReader<Genre> genreReader() {
        MongoItemRestartableReader<Genre> reader = new MongoItemRestartableReader<>();
        reader.setName("genreReader");
        reader.setTemplate(mongoTemplate);
        reader.setQuery("{}");
        reader.setTargetType(Genre.class);
        reader.setSort(new HashMap<>());
        return reader;
    }

    @Bean
    public ItemProcessor<Genre, Genre> genreProcessor() {
        return genre -> genre;
    }

    @Bean
    public JdbcBatchItemWriter<Genre> genreWriter() {
        return new JdbcBatchItemWriterBuilder<Genre>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("insert into genre (id, title) values (:id, :title)")
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public Step copyGenresStep(MongoItemRestartableReader<Genre> genreReader) {
        return stepBuilderFactory.get("copyGenresStep")
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    genreReader.setPage(0); // ???????????????????? ?????? ???????????????? ?????????? (?????????? ???????????????? ?????????? ????????????????, ?? ?????????????? ???????????????? ????????????)
                }

                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    return stepExecution.getExitStatus();
                }
            })
            .<Genre, Genre>chunk(CHUNK_SIZE)
            .reader(genreReader)
            .processor(genreProcessor())
            .writer(genreWriter())
            .listener(new ItemReadListener<>() {
                public void beforeRead() {
                    logger.info("???????????? ???????????? ??????????");
                }

                public void afterRead(@NonNull Genre o) {
                    logger.info("?????????? ???????????? ?????????? {}", o.getId());
                }

                public void onReadError(@NonNull Exception e) {
                    logger.error("???????????? ???????????? ??????????: {}", e.getMessage());
                }
            })
            .listener(new ItemWriteListener<>() {
                public void beforeWrite(@NonNull List list) {
                    logger.info("???????????? ???????????? ??????????");
                }

                public void afterWrite(@NonNull List list) {
                    logger.info("?????????? ???????????? ??????????");
                }

                public void onWriteError(@NonNull Exception e, @NonNull List list) {
                    logger.error("???????????? ???????????? ??????????: {}", e.getMessage());
                }
            })
            .listener(new ItemProcessListener<>() {
                public void beforeProcess(Genre o) {
                    logger.info("???????????? ?????????????????? ?????????? {}", o.getId());
                }

                public void afterProcess(@NonNull Genre o, Genre o2) {
                    logger.info("?????????? ?????????????????? ?????????? {}", o.getId());
                }

                public void onProcessError(@NonNull Genre o, @NonNull Exception e) {
                    logger.error("???????????? ?????????????????? ?????????? {}: {}", o.getId(), e.getMessage());
                }
            })
            .listener(new ChunkListener() {
                public void beforeChunk(@NonNull ChunkContext chunkContext) {
                    logger.info("???????????? ?????????? ??????????");
                }

                public void afterChunk(@NonNull ChunkContext chunkContext) {
                    logger.info("?????????? ?????????? ??????????");
                }

                public void afterChunkError(@NonNull ChunkContext chunkContext) {
                    logger.error("???????????? ?????????? ??????????");
                }
            })
            .taskExecutor(new SimpleAsyncTaskExecutor())
            .build();
    }
}
