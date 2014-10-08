package RDG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import Entity.Answer;
import Entity.Question;
import Tools.IPersistableWithId;

public class QuestionRdg implements IPersistableWithId<Question>{
	
	public static final String REQUEST_PERSIST = "insert into Question(description) values(?)";
	public static final String REQUEST_RETRIEVE = "select * from Question where Question.id = ?";
	public static final String REQUEST_RETRIEVE_ANSWERS = "select idAnswer from Question where Question.id = ?";
	public static final String REQUEST_UPDATE = "update Question set description = ? where Question.id = ?";
	public static final String REQUEST_DELETE = "delete from Question where Question.id = ?";
	
	private Connection connection;
	private AnswerRdg answerRdg;
	
	public QuestionRdg(Connection connection) {
		this.connection = connection;
		this.answerRdg = new AnswerRdg(connection);
	}

	@Override
	public void persist(Question obj) throws SQLException {
		// TODO Auto-generated method stub
		if(obj.getId() != null) {
			update(obj);
			return;
		}
		try {
			this.connection.setAutoCommit(false);
			persistAnswers(obj.getAnswers());
			PreparedStatement statement = this.connection.prepareStatement(REQUEST_PERSIST, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, obj.getDesc());
			statement.executeUpdate();
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if(!generatedKeys.next()) throw new IllegalStateException("no generated keys");
			obj.setId(generatedKeys.getInt(1));
		} catch(Exception e) {
			this.connection.rollback();
			this.connection.setAutoCommit(true);
			throw e;
		}
	}

	@Override
	public void update(Question obj) throws SQLException {
		// TODO Auto-generated method stub
		if(obj.getId() == null) {
			persist(obj);
			return;
		}
		try {
			this.connection.setAutoCommit(false);
			updateAnswers(obj.getAnswers());
			PreparedStatement statement = this.connection.prepareStatement(REQUEST_UPDATE);
			statement.setString(1, obj.getDesc());
			statement.executeUpdate();
		} catch(Exception e) {
			this.connection.rollback();
			this.connection.setAutoCommit(true);
			throw e;
		}
	}

	@Override
	public void delete(Question obj) throws SQLException {
		// TODO Auto-generated method stub
		try {
			this.connection.setAutoCommit(false);
			deleteAnswers(obj.getAnswers());
			PreparedStatement statement = this.connection.prepareStatement(REQUEST_DELETE);
			statement.setInt(1, obj.getId());
			statement.executeUpdate();
		} catch(Exception e) {
			this.connection.rollback();
			this.connection.setAutoCommit(true);
			throw e;
		}
	}

	@Override
	public Question retrieve(Integer id) throws SQLException {
		// TODO Auto-generated method stub
		Question question = null;
		try {
			this.connection.setAutoCommit(false);
			PreparedStatement statement = this.connection.prepareStatement(REQUEST_RETRIEVE);
			statement.setInt(1, id);
			ResultSet set = statement.executeQuery();
			if(set.next()) {
				question = new Question();
				question.setId(id);
				question.setDesc(set.getString(2));
				question.setAnswers(retrieveAnswers(id));
			}
		} catch(Exception e) {
			this.connection.rollback();
			this.connection.setAutoCommit(true);
			throw e;
		}
		return question;
	}
	
	public void increaseAnswer(Answer answer) throws SQLException {
		Integer cpt = answer.getCpt();
		answer.setCpt(cpt++);
		answerRdg.update(answer);
	}
	
	private void persistAnswers(Collection<Answer> answers) throws SQLException {
		for(Answer a : answers) {
			answerRdg.persist(a);
		}
	}
	
	private void updateAnswers(Collection<Answer> answers) throws SQLException {
		for(Answer a : answers) {
			answerRdg.update(a);
		}
	}
	
	private void deleteAnswers(Collection<Answer> answers) throws SQLException {
		for(Answer a : answers) {
			answerRdg.delete(a);
		}
	}
	
	private Collection<Answer> retrieveAnswers(Integer id) throws SQLException {
		Collection<Answer> answers = new ArrayList<Answer>();
		PreparedStatement statement = this.connection.prepareStatement(REQUEST_RETRIEVE_ANSWERS);
		statement.setInt(1, id);
		ResultSet set = statement.executeQuery();
		while(set.next()) {
			answers.add(answerRdg.retrieve(set.getInt(1)));
		}
		return answers;
	}

}
