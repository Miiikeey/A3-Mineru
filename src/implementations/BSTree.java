package implementations;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Stack;
import utilities.BSTreeADT;
import utilities.Iterator;

public class BSTree<E extends Comparable<? super E>> implements BSTreeADT<E>, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BSTreeNode<E> root;
    private int size;

    public BSTree() {
        this.root = null;
        this.size = 0;
    }

    @Override
    public BSTreeNode<E> getRoot() throws NullPointerException {
        if (isEmpty()) throw new NullPointerException("Tree is empty");
        return root;
    }

    @Override
    public int getHeight() {
        return calculateHeight(root);
    }

    private int calculateHeight(BSTreeNode<E> node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateHeight(node.getLeft()), calculateHeight(node.getRight()));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean contains(E entry) throws NullPointerException {
        if (entry == null) throw new NullPointerException("Entry is null");
        return search(entry) != null;
    }

    @Override
    public BSTreeNode<E> search(E entry) throws NullPointerException {
        if (entry == null) throw new NullPointerException("Entry is null");
        return searchRecursive(root, entry);
    }

    private BSTreeNode<E> searchRecursive(BSTreeNode<E> node, E entry) {
        if (node == null) return null;
        int compare = entry.compareTo(node.getElement());
        if (compare == 0) return node;
        else if (compare < 0) return searchRecursive(node.getLeft(), entry);
        else return searchRecursive(node.getRight(), entry);
    }

    @Override
    public boolean add(E newEntry) throws NullPointerException {
        if (newEntry == null) throw new NullPointerException("Entry is null");
        if (root == null) {
            root = new BSTreeNode<>(newEntry);
            size++;
            return true;
        }
        return addNode(root, newEntry);
    }

    private boolean addNode(BSTreeNode<E> node, E newEntry) {
    	int compare = newEntry.compareTo(node.getElement());
        if (compare == 0) return false;
        else if (compare < 0) {
            if (node.getLeft() == null) {
                node.setLeft(new BSTreeNode<>(newEntry));
                size++;
                return true;
            }
            return addNode(node.getLeft(), newEntry);
        } else {
            if (node.getRight() == null) {
                node.setRight(new BSTreeNode<>(newEntry));
                size++;
                return true;
            }
            return addNode(node.getRight(), newEntry);
        }
    }

    @Override
    public BSTreeNode<E> removeMin() {
        if (isEmpty()) return null;
        BSTreeNode<E> minNode = root;
        BSTreeNode<E> parent = null;
        while (minNode.getLeft() != null) {
            parent = minNode;
            minNode = minNode.getLeft();
        }
        if (parent == null) root = root.getRight();
        else parent.setLeft(minNode.getRight());
        size--;
        return minNode;
    }

    @Override
    public BSTreeNode<E> removeMax() {
        if (isEmpty()) return null;
        BSTreeNode<E> maxNode = root;
        BSTreeNode<E> parent = null;
        while (maxNode.getRight() != null) {
            parent = maxNode;
            maxNode = maxNode.getRight();
        }
        if (parent == null) root = root.getLeft();
        else parent.setRight(maxNode.getLeft());
        size--;
        return maxNode;
    }

    @Override
    public Iterator<E> inorderIterator() {
        return new BSTIterator(root, "inorder");
    }

    @Override
    public Iterator<E> preorderIterator() {
        return new BSTIterator(root, "preorder");
    }

    @Override
    public Iterator<E> postorderIterator() {
        return new BSTIterator(root, "postorder");
    }

    /**
     * Inner class BSTIterator to implement traversals.
     */
    private class BSTIterator implements Iterator<E> {
        private Stack<BSTreeNode<E>> stack;
        private String traversalType;

        public BSTIterator(BSTreeNode<E> root, String traversalType) {
            this.stack = new Stack<>();
            this.traversalType = traversalType.toLowerCase();

            if (traversalType.equals("inorder")) {
                pushLeft(root);
            } else if (traversalType.equals("preorder")) {
                if (root != null) stack.push(root);
            } else if (traversalType.equals("postorder")) {
                pushPostOrder(root);
            } else {
                throw new IllegalArgumentException("Invalid traversal type: " + traversalType);
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public E next() throws NoSuchElementException {
            if (!hasNext()) throw new NoSuchElementException("No more elements to iterate over");

            if (traversalType.equals("inorder")) {
                BSTreeNode<E> node = stack.pop();
                E result = node.getElement();
                if (node.getRight() != null) pushLeft(node.getRight());
                return result;
            } else if (traversalType.equals("preorder")) {
                BSTreeNode<E> node = stack.pop();
                E result = node.getElement();
                if (node.getRight() != null) stack.push(node.getRight());
                if (node.getLeft() != null) stack.push(node.getLeft());
                return result;
            } else { 
                BSTreeNode<E> node = stack.pop();
                return node.getElement();
            }
        }

        private void pushLeft(BSTreeNode<E> node) {
            while (node != null) {
                stack.push(node);
                node = node.getLeft();
            }
        }

        private void pushPostOrder(BSTreeNode<E> root) {
            Stack<BSTreeNode<E>> tempStack = new Stack<>();
            tempStack.push(root);
            while (!tempStack.isEmpty()) {
                BSTreeNode<E> node = tempStack.pop();
                if (node != null) {
                    stack.push(node);
                    tempStack.push(node.getLeft());
                    tempStack.push(node.getRight());
                }
            }
        }
    }
}
