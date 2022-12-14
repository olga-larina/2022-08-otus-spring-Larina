package ru.otus.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.domain.Answer;
import ru.otus.domain.Question;
import ru.otus.loader.ResourceLoader;
import ru.otus.parser.EntityParserCsv;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuestionDaoCsvUnitTest {

    @Mock
    private ResourceLoader<String[]> resourceLoader;

    @Mock
    private EntityParserCsv<Question> questionParser;

    @InjectMocks
    private QuestionDaoCsv questionDao;

    @Test
    public void shouldReturnCorrectQuestions() {
        List<Question> questions = List.of(
            new Question("What is your name?", List.of(new Answer(1, "Vasya", true), new Answer(2, "Petya", false))),
            new Question("How old are you?", List.of(new Answer(1, "18", false), new Answer(2, "20", true), new Answer(3, "25", false)))
        );
        List<String[]> lines = List.of(
            new String[] { questions.get(0).getText(), "2", "1", questions.get(0).getAnswers().get(0).getText(), questions.get(0).getAnswers().get(1).getText() },
            new String[] { questions.get(1).getText(), "3", "2", questions.get(1).getAnswers().get(0).getText(), questions.get(1).getAnswers().get(1).getText(), questions.get(1).getAnswers().get(2).getText() }
        );
        when(resourceLoader.loadData()).thenReturn(lines);
        when(questionParser.parse(lines.get(0))).thenReturn(questions.get(0));
        when(questionParser.parse(lines.get(1))).thenReturn(questions.get(1));
        assertThat(questionDao.findAllQuestions()).isEqualTo(questions);
    }
}
