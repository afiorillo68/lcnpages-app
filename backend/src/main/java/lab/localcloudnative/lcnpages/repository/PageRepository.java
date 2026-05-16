package lab.localcloudnative.lcnpages.repository;

import lab.localcloudnative.lcnpages.domain.Page;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends MongoRepository<Page, ObjectId> {
    List<Page> findByOwnerId(String ownerId);
    List<Page> findByOwnerIdAndParentId(String ownerId, ObjectId parentId);
    Optional<Page> findByOwnerIdAndId(String ownerId, ObjectId id);
    List<Page> findByOwnerIdAndTitleContainingIgnoreCase(String ownerId, String title);
    List<Page> findByOwnerIdAndTagsContaining(String ownerId, String tag);
}
