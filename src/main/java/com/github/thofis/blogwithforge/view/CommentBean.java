package com.github.thofis.blogwithforge.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.github.thofis.blogwithforge.entities.Comment;
import com.github.thofis.blogwithforge.entities.BlogEntry;

/**
 * Backing bean for Comment entities.
 * <p>
 * This class provides CRUD functionality for all Comment entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class CommentBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Comment entities
    */

   private Long id;

   public Long getId()
   {
      return this.id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   private Comment comment;

   public Comment getComment()
   {
      return this.comment;
   }

   @Inject
   private Conversation conversation;

   @PersistenceContext(type = PersistenceContextType.EXTENDED)
   private EntityManager entityManager;

   public String create()
   {

      this.conversation.begin();
      return "create?faces-redirect=true";
   }

   public void retrieve()
   {

      if (FacesContext.getCurrentInstance().isPostback())
      {
         return;
      }

      if (this.conversation.isTransient())
      {
         this.conversation.begin();
      }

      if (this.id == null)
      {
         this.comment = this.example;
      }
      else
      {
         this.comment = findById(getId());
      }
   }

   public Comment findById(Long id)
   {

      return this.entityManager.find(Comment.class, id);
   }

   /*
    * Support updating and deleting Comment entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.comment);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.comment);
            return "view?faces-redirect=true&id=" + this.comment.getId();
         }
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   public String delete()
   {
      this.conversation.end();

      try
      {
         Comment deletableEntity = findById(getId());
         BlogEntry blogEntry = deletableEntity.getBlogEntry();
         blogEntry.getComments().remove(deletableEntity);
         deletableEntity.setBlogEntry(null);
         this.entityManager.merge(blogEntry);
         this.entityManager.remove(deletableEntity);
         this.entityManager.flush();
         return "search?faces-redirect=true";
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   /*
    * Support searching Comment entities with pagination
    */

   private int page;
   private long count;
   private List<Comment> pageItems;

   private Comment example = new Comment();

   public int getPage()
   {
      return this.page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public int getPageSize()
   {
      return 10;
   }

   public Comment getExample()
   {
      return this.example;
   }

   public void setExample(Comment example)
   {
      this.example = example;
   }

   public void search()
   {
      this.page = 0;
   }

   public void paginate()
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

      // Populate this.count

      CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
      Root<Comment> root = countCriteria.from(Comment.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Comment> criteria = builder.createQuery(Comment.class);
      root = criteria.from(Comment.class);
      TypedQuery<Comment> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Comment> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      String originator = this.example.getOriginator();
      if (originator != null && !"".equals(originator))
      {
         predicatesList.add(builder.like(root.<String> get("originator"), '%' + originator + '%'));
      }
      String content = this.example.getContent();
      if (content != null && !"".equals(content))
      {
         predicatesList.add(builder.like(root.<String> get("content"), '%' + content + '%'));
      }
      BlogEntry blogEntry = this.example.getBlogEntry();
      if (blogEntry != null)
      {
         predicatesList.add(builder.equal(root.get("blogEntry"), blogEntry));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Comment> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Comment entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Comment> getAll()
   {

      CriteriaQuery<Comment> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Comment.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Comment.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final CommentBean ejbProxy = this.sessionContext.getBusinessObject(CommentBean.class);

      return new Converter()
      {

         @Override
         public Object getAsObject(FacesContext context,
               UIComponent component, String value)
         {

            return ejbProxy.findById(Long.valueOf(value));
         }

         @Override
         public String getAsString(FacesContext context,
               UIComponent component, Object value)
         {

            if (value == null)
            {
               return "";
            }

            return String.valueOf(((Comment) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Comment add = new Comment();

   public Comment getAdd()
   {
      return this.add;
   }

   public Comment getAdded()
   {
      Comment added = this.add;
      this.add = new Comment();
      return added;
   }
}