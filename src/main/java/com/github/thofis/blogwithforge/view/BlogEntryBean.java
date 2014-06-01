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

import com.github.thofis.blogwithforge.entities.BlogEntry;
import com.github.thofis.blogwithforge.entities.Blogger;
import com.github.thofis.blogwithforge.entities.Comment;
import java.util.Iterator;

/**
 * Backing bean for BlogEntry entities.
 * <p>
 * This class provides CRUD functionality for all BlogEntry entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class BlogEntryBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving BlogEntry entities
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

   private BlogEntry blogEntry;

   public BlogEntry getBlogEntry()
   {
      return this.blogEntry;
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
         this.blogEntry = this.example;
      }
      else
      {
         this.blogEntry = findById(getId());
      }
   }

   public BlogEntry findById(Long id)
   {

      return this.entityManager.find(BlogEntry.class, id);
   }

   /*
    * Support updating and deleting BlogEntry entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.blogEntry);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.blogEntry);
            return "view?faces-redirect=true&id=" + this.blogEntry.getId();
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
         BlogEntry deletableEntity = findById(getId());
         Blogger blogger = deletableEntity.getBlogger();
         blogger.getBlogEntries().remove(deletableEntity);
         deletableEntity.setBlogger(null);
         this.entityManager.merge(blogger);
         Iterator<Comment> iterComments = deletableEntity.getComments().iterator();
         for (; iterComments.hasNext();)
         {
            Comment nextInComments = iterComments.next();
            nextInComments.setBlogEntry(null);
            iterComments.remove();
            this.entityManager.merge(nextInComments);
         }
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
    * Support searching BlogEntry entities with pagination
    */

   private int page;
   private long count;
   private List<BlogEntry> pageItems;

   private BlogEntry example = new BlogEntry();

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

   public BlogEntry getExample()
   {
      return this.example;
   }

   public void setExample(BlogEntry example)
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
      Root<BlogEntry> root = countCriteria.from(BlogEntry.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<BlogEntry> criteria = builder.createQuery(BlogEntry.class);
      root = criteria.from(BlogEntry.class);
      TypedQuery<BlogEntry> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<BlogEntry> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      String title = this.example.getTitle();
      if (title != null && !"".equals(title))
      {
         predicatesList.add(builder.like(root.<String> get("title"), '%' + title + '%'));
      }
      String content = this.example.getContent();
      if (content != null && !"".equals(content))
      {
         predicatesList.add(builder.like(root.<String> get("content"), '%' + content + '%'));
      }
      Blogger blogger = this.example.getBlogger();
      if (blogger != null)
      {
         predicatesList.add(builder.equal(root.get("blogger"), blogger));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<BlogEntry> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back BlogEntry entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<BlogEntry> getAll()
   {

      CriteriaQuery<BlogEntry> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(BlogEntry.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(BlogEntry.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final BlogEntryBean ejbProxy = this.sessionContext.getBusinessObject(BlogEntryBean.class);

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

            return String.valueOf(((BlogEntry) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private BlogEntry add = new BlogEntry();

   public BlogEntry getAdd()
   {
      return this.add;
   }

   public BlogEntry getAdded()
   {
      BlogEntry added = this.add;
      this.add = new BlogEntry();
      return added;
   }
}