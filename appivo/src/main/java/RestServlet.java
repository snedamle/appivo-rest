import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import static com.mongodb.client.model.Filters.eq;

@WebServlet("/api")
public class RestServlet extends HttpServlet {

	private MongoDatabase database;
	private MongoCollection<Document> collection;

	@Override
	public void init() throws ServletException {
		MongoClient mongoClient = MongoClients.create();

		database = mongoClient.getDatabase("mydb");

		collection = database.getCollection("Employee");

		super.init();
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		LinkedTreeMap<String, Object> person = new Gson().fromJson(req.getReader(), LinkedTreeMap.class);

		Document document = new Document();

		for (String key : person.keySet()) {
			document.append(key, person.get(key));
		}

		collection.updateOne(eq("id", req.getParameter("id")), new Document("$set", document),
                new SingleResultCallback<UpdateResult>() {
                    @Override
                    public void onResult(final UpdateResult result, final Throwable t) {
                    	if(result != null) {
                            System.out.println(result.getModifiedCount());
                    	}
                    }
        });

		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		out.print("Updating data");
		
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		collection.deleteOne(eq("id", req.getParameter("id")), new SingleResultCallback<DeleteResult>() {
			@Override
			public void onResult(final DeleteResult result, final Throwable t) {
				if(result != null) {
					System.out.println("Deleted" + result.getDeletedCount());
				}
			}
		});
		
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		out.print("Deleting data");

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LinkedTreeMap<String, Object> person = new Gson().fromJson(request.getReader(), LinkedTreeMap.class);

		// find first
		SingleResultCallback<Document> printDocument = new SingleResultCallback<Document>() {
			@Override
			public void onResult(final Document document, final Throwable t) {
				if(document != null) {
					System.out.println(document.toJson());
				}
			}
		};
		collection.find(eq("id", request.getParameter("id"))).first(printDocument);
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("Getting data");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LinkedTreeMap<String, Object> person = new Gson().fromJson(request.getReader(), LinkedTreeMap.class);

		Document document = new Document();

		for (String key : person.keySet()) {
			document.append(key, person.get(key));
		}

		collection.insertOne(document, new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				System.out.println("Inserted!" + document.get("_id"));
			}
		});

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print("Inserting Data");
	}
}
