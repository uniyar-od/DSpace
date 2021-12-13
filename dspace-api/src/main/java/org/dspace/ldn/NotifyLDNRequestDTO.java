package org.dspace.ldn;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NotifyLDNRequestDTO {

	@JsonProperty("actor")
	private Actor actor;

	@JsonProperty("context")
	private Context context;

	@JsonProperty("id")
	private String id;

	@JsonProperty("object")
	private Object object;

	@JsonProperty("origin")
	private Origin origin;

	@JsonProperty("target")
	private Target target;

	@JsonProperty("type")
	private String[] type;
	
	@JsonProperty("inReplyTo")
	private String inReplyTo;
	

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public Actor getActor() {
		return actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}

	public String[] getType() {
		return type;
	}

	public void setType(String[] type) {
		this.type = type;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Context {

		@JsonProperty("id")
		private String id;

		@JsonProperty("ietf:cite-as")
		private String ietfCiteAs;

		@JsonProperty("type")
		private String type;

		@JsonProperty("url")
		private Url url;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getIetfCiteAs() {
			return ietfCiteAs;
		}

		public void setIetfCiteAs(String ietfCiteAs) {
			this.ietfCiteAs = ietfCiteAs;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Url getUrl() {
			return url;
		}

		public void setUrl(Url url) {
			this.url = url;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public class Url {

			@JsonProperty("id")
			private String id;

			@JsonProperty("media-type")
			private String mediaType;

			@JsonProperty("type")
			private String[] type;

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getMediaType() {
				return mediaType;
			}

			public void setMediaType(String mediaType) {
				this.mediaType = mediaType;
			}

			public String[] getType() {
				return type;
			}

			public void setType(String[] type) {
				this.type = type;
			}

		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Actor {

		@JsonProperty("id")
		private String id;

		@JsonProperty("name")
		private String name;

		@JsonProperty("type")
		private String type;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Object {

		@JsonProperty("id")
		private String id;

		@JsonProperty("ietf:cite-as")
		private String ietfCiteAs;

		@JsonProperty("type")
		private String[] type;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getIetfCiteAs() {
			return ietfCiteAs;
		}

		public void setIetfCiteAs(String ietfCiteAs) {
			this.ietfCiteAs = ietfCiteAs;
		}

		public String[] getType() {
			return type;
		}

		public void setType(String[] type) {
			this.type = type;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Origin {

		@JsonProperty("id")
		private String id;

		@JsonProperty("inbox")
		private String inbox;

		@JsonProperty("type")
		private String type;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getInbox() {
			return inbox;
		}

		public void setInbox(String inbox) {
			this.inbox = inbox;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Target {

		@JsonProperty("id")
		private String id;

		@JsonProperty("inbox")
		private String inbox;

		@JsonProperty("type")
		private String type;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getInbox() {
			return inbox;
		}

		public void setInbox(String inbox) {
			this.inbox = inbox;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}
